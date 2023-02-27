package com.yqhp.agent.devicediscovery.ios;

import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.agent.devicediscovery.DeviceDiscovery;

import java.time.Duration;

/**
 * @author jiangyitao
 */
public class IOSEmulatorDiscovery extends DeviceDiscovery {

    private final Duration scanPeriod;

    public IOSEmulatorDiscovery(Duration scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    @Override
    protected void run(DeviceChangeListener listener) {
        IOSEmulatorScheduledScanner.getInstance().start(scanPeriod, listener);
    }

    @Override
    protected void terminate() {
        IOSEmulatorScheduledScanner.getInstance().stop();
    }
}
