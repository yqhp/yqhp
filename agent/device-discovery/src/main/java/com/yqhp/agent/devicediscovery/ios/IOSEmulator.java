package com.yqhp.agent.devicediscovery.ios;

import lombok.Getter;

/**
 * @author jiangyitao
 */
public class IOSEmulator extends IOSDevice {

    @Getter
    private final String model;

    IOSEmulator(String model, String udid) {
        super(udid, true);
        this.model = model;
    }
}
