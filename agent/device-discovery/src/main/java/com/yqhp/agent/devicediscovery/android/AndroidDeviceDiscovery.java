package com.yqhp.agent.devicediscovery.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.yqhp.agent.androidtools.ADB;
import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.agent.devicediscovery.DeviceDiscovery;

import java.time.Duration;

/**
 * @author jiangyitao
 */
public class AndroidDeviceDiscovery extends DeviceDiscovery {

    private final String adbPath;
    private final Duration adbInitTimeout;

    public AndroidDeviceDiscovery(String adbPath, Duration adbInitTimeout) {
        this.adbPath = adbPath;
        this.adbInitTimeout = adbInitTimeout;
    }

    @Override
    protected void run(DeviceChangeListener listener) {
        ADB.addDeviceChangeListener((AndroidDebugBridge.IDeviceChangeListener) listener);
        ADB.init(adbPath, adbInitTimeout);
    }

    @Override
    protected void terminate() {
        ADB.terminate();
    }
}
