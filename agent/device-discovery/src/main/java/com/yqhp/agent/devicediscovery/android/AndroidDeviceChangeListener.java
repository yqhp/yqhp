package com.yqhp.agent.devicediscovery.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jiangyitao
 */
@Slf4j
public abstract class AndroidDeviceChangeListener
        implements AndroidDebugBridge.IDeviceChangeListener, DeviceChangeListener {

    @Override
    public void deviceConnected(IDevice device) {
        log.info("[{}]deviceConnected, deviceState={}", device.getSerialNumber(), device.getState());
        if (device.isOnline()) {
            online(new AndroidDevice(device));
        }
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        log.info("[{}]deviceDisconnected, deviceState={}", device.getSerialNumber(), device.getState());
        offline(new AndroidDevice(device));
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        log.info("[{}]deviceChanged, deviceState={}, changeMask={}",
                device.getSerialNumber(), device.getState(), changeMask);
        if (changeMask == IDevice.CHANGE_STATE) {
            if (device.isOnline()) {
                online(new AndroidDevice(device));
            } else {
                offline(new AndroidDevice(device));
            }
        }
    }
}
