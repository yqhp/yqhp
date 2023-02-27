package com.yqhp.agent.usbmuxd;

/**
 * @author jiangyitao
 */
public interface IDeviceChangeListener {
    void deviceConnected(IDevice device);

    void deviceDisconnected(IDevice device);
}
