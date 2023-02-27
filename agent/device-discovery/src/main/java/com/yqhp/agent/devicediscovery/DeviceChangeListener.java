package com.yqhp.agent.devicediscovery;

/**
 * @author jiangyitao
 */
public interface DeviceChangeListener {
    void online(Device device);

    void offline(Device device);
}
