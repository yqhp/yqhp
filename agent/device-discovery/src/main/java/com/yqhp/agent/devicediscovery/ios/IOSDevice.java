package com.yqhp.agent.devicediscovery.ios;

import com.yqhp.agent.devicediscovery.Device;

/**
 * @author jiangyitao
 */
public class IOSDevice extends Device {

    IOSDevice(String udid, boolean isEmulator) {
        super(udid, isEmulator);
    }
}
