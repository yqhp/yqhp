package com.yqhp.agent.devicediscovery;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author jiangyitao
 */
@Getter
@EqualsAndHashCode(of = {"id"})
@ToString
public abstract class Device {

    protected String id;
    protected boolean isEmulator;

    public Device(String id, boolean isEmulator) {
        this.id = id;
        this.isEmulator = isEmulator;
    }
}
