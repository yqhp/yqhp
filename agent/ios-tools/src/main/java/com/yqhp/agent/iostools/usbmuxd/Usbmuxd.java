/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.agent.iostools.usbmuxd;

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
        try (UsbmuxdChannel channel = new UsbmuxdChannel()) {
            NSDictionary req = new NSDictionary();
            req.put("MessageType", "ListDevices");
            req.put("ClientVersionString", CLIENT_VERSION);
            req.put("ProgName", PROG_NAME);

            NSDictionary response = (NSDictionary) channel.writeAndRead(req);
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
            throw new IllegalStateException("DevicesListener is listening");
        }
        devicesListening = true;
        new Thread(() -> {
            try (UsbmuxdChannel channel = new UsbmuxdChannel()) {
                // 发送监听设备请求
                NSDictionary req = new NSDictionary();
                req.put("MessageType", "Listen");
                req.put("ClientVersionString", CLIENT_VERSION);
                req.put("ProgName", PROG_NAME);
                channel.write(req);

                // deviceId : IDevice
                Map<Long, IDevice> connectedDevices = new HashMap<>();

                while (devicesListening) {
                    NSObject response = channel.read(); // 读取数据，没有数据将阻塞在此
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