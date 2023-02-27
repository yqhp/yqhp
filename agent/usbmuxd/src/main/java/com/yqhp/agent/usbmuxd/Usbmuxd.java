package com.yqhp.agent.usbmuxd;

import com.dd.plist.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jiangyitao
 */
@Slf4j
public class Usbmuxd {

    private static final String CLIENT_VERSION = "v0.0.1";
    private static final String PROG_NAME = "yqhp-agent";

    private static final AtomicInteger TAG = new AtomicInteger(1);

    private volatile boolean listening = false;

    public List<IDevice> getDeviceList() {
        try (UsbmuxdConnection connection = new UsbmuxdConnection()) {
            return getDeviceList(connection);
        }
    }

    private List<IDevice> getDeviceList(UsbmuxdConnection connection) {
        NSDictionary requestData = new NSDictionary();
        requestData.put("MessageType", "ListDevices");
        requestData.put("ClientVersionString", CLIENT_VERSION);
        requestData.put("ProgName", PROG_NAME);

        NSDictionary response = (NSDictionary) doRequest(connection, requestData);
        NSArray deviceList = (NSArray) response.get("DeviceList");
        return Stream.of(deviceList.getArray())
                .map(device -> {
                    NSObject properties = ((NSDictionary) device).get("Properties");
                    return new IDevice((NSDictionary) properties);
                }).collect(Collectors.toList());
    }

    public synchronized void startListenDevices(IDeviceChangeListener listener) {
        startListenDevices(listener, null);
    }

    public synchronized void startListenDevices(IDeviceChangeListener listener, Executor executor) {
        Runnable runnable = () -> {
            try (UsbmuxdConnection connection = new UsbmuxdConnection()) {
                startListenDevices(connection, listener);
            }
        };

        if (executor == null) {
            new Thread(runnable).start();
        } else {
            executor.execute(runnable);
        }
    }

    public synchronized void startListenDevices(UsbmuxdConnection connection, IDeviceChangeListener listener) {
        if (listening) {
            throw new IllegalStateException("listening");
        }

        NSDictionary requestData = new NSDictionary();
        requestData.put("MessageType", "Listen");
        requestData.put("ClientVersionString", CLIENT_VERSION);
        requestData.put("ProgName", PROG_NAME);

        doRequest(connection.getOutputStream(), requestData);

        // deviceId : IDevice
        Map<Long, IDevice> connectedDevices = new HashMap<>();

        InputStream inputStream = connection.getInputStream();
        listening = true;
        while (listening) {
            NSObject response = readResponse(inputStream);
            if (response instanceof NSDictionary) {
                NSDictionary res = (NSDictionary) response;
                NSObject messageType = res.get("MessageType");
                if (messageType instanceof NSString) {
                    String msgType = messageType.toString();
                    if (msgType.equals("Attached")) {
                        NSDictionary properties = (NSDictionary) res.get("Properties");
                        IDevice iDevice = new IDevice(properties);
                        connectedDevices.put(iDevice.getDeviceId(), iDevice);
                        listener.deviceConnected(iDevice);
                    } else if (msgType.equals("Detached")) {
                        Long deviceId = ((NSNumber) res.get("DeviceID")).longValue();
                        IDevice iDevice = connectedDevices.get(deviceId);
                        if (iDevice != null) {
                            listener.deviceDisconnected(iDevice);
                            connectedDevices.remove(deviceId);
                        }
                    }
                }
            }
        }
    }

    public synchronized void stopListenDevices() {
        if (listening) {
            listening = false;
        }
    }

    private NSObject doRequest(UsbmuxdConnection connection, NSObject requestData) {
        doRequest(connection.getOutputStream(), requestData);
        return readResponse(connection.getInputStream());
    }

    private void doRequest(OutputStream outputStream, NSObject requestData) {
        byte[] payload = requestData.toXMLPropertyList()
                .getBytes(StandardCharsets.UTF_8);

        int headerLen = 16;
        int packetLen = headerLen + payload.length;

        ByteBuffer packet = ByteBuffer.allocate(packetLen);
        packet.order(ByteOrder.LITTLE_ENDIAN);

        // header
        packet.putInt(0, packetLen);
        packet.putInt(4, 1);
        packet.putInt(8, 8);
        packet.putInt(12, TAG.getAndIncrement());

        // payload
        int i = headerLen;
        for (byte b : payload) {
            packet.put(i++, b);
        }

        try {
            outputStream.write(packet.array());
        } catch (IOException e) {
            throw new UsbmuxdException(e);
        }
    }

    private NSObject readResponse(InputStream inputStream) {
        try {
            byte[] header = new byte[16];
            inputStream.read(header);
            ByteBuffer headerBuffer = ByteBuffer.wrap(header);
            headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int payloadLen = headerBuffer.getInt(0) - 16;

            byte[] payload = new byte[payloadLen];
            inputStream.read(payload);
            return PropertyListParser.parse(payload);
        } catch (Exception e) {
            throw new UsbmuxdException(e);
        }
    }
}
