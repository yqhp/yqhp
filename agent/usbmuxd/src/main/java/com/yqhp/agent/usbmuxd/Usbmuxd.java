package com.yqhp.agent.usbmuxd;

import com.dd.plist.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jiangyitao
 */
@Slf4j
public class Usbmuxd {

    private static final String CLIENT_VERSION = "v0.0.1";
    private static final String PROG_NAME = "yqhp-usbmuxd";

    private volatile boolean devicesListening = false;

    public List<IDevice> listDevices() {
        try (UsbmuxdConnection conn = new UsbmuxdConnection()) {
            NSDictionary req = new NSDictionary();
            req.put("MessageType", "ListDevices");
            req.put("ClientVersionString", CLIENT_VERSION);
            req.put("ProgName", PROG_NAME);

            NSDictionary response = (NSDictionary) conn.writeAndRead(req);
            NSArray deviceList = (NSArray) response.get("DeviceList");
            return Stream.of(deviceList.getArray())
                    .map(device -> {
                        NSObject properties = ((NSDictionary) device).get("Properties");
                        return new IDevice((NSDictionary) properties);
                    }).collect(Collectors.toList());
        }
    }

    public synchronized void startListenDevices(IDeviceChangeListener listener) {
        if (devicesListening) {
            throw new IllegalStateException("listening");
        }
        devicesListening = true;
        new Thread(() -> {
            try (UsbmuxdConnection conn = new UsbmuxdConnection()) {
                // 发送监听设备请求
                NSDictionary req = new NSDictionary();
                req.put("MessageType", "Listen");
                req.put("ClientVersionString", CLIENT_VERSION);
                req.put("ProgName", PROG_NAME);
                conn.write(req);

                // deviceId : IDevice
                Map<Long, IDevice> connectedDevices = new HashMap<>();

                while (devicesListening) {
                    NSObject response = conn.read(); // 读取数据，没有数据将阻塞在此
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
        }).start();
    }

    public synchronized void stopListenDevices() {
        if (devicesListening) {
            devicesListening = false;
        }
    }
}