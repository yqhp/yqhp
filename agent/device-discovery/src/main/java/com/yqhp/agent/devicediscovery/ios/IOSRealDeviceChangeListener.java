package com.yqhp.agent.devicediscovery.ios;

import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.agent.usbmuxd.IDevice;
import com.yqhp.agent.usbmuxd.IDeviceChangeListener;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jiangyitao
 */
@Slf4j
public abstract class IOSRealDeviceChangeListener
        implements IDeviceChangeListener, DeviceChangeListener {

    @Override
    public void deviceConnected(IDevice device) {
        log.info("[{}]deviceConnected", device.getSerialNumber());
        online(new IOSRealDevice(device));
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        log.info("[{}]deviceDisconnected", device.getSerialNumber());
        offline(new IOSRealDevice(device));
    }
}
