package com.yqhp.agent.devicediscovery.ios;

import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.agent.devicediscovery.DeviceDiscovery;
import com.yqhp.agent.usbmuxd.IDeviceChangeListener;
import com.yqhp.agent.usbmuxd.Usbmuxd;

/**
 * @author jiangyitao
 */
public class IOSRealDeviceDiscovery extends DeviceDiscovery {

    private final Usbmuxd usbmuxd = new Usbmuxd();

    @Override
    protected void run(DeviceChangeListener listener) {
        usbmuxd.startListenDevices((IDeviceChangeListener) listener);
    }

    @Override
    protected void terminate() {
        usbmuxd.stopListenDevices();
    }
}
