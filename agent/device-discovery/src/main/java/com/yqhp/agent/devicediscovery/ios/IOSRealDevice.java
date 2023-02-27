package com.yqhp.agent.devicediscovery.ios;

import com.yqhp.agent.usbmuxd.IDevice;
import lombok.Getter;

/**
 * @author jiangyitao
 */
public class IOSRealDevice extends IOSDevice {

    @Getter
    private final IDevice iDevice;

    IOSRealDevice(IDevice iDevice) {
        super(iDevice.getSerialNumber(), false);
        this.iDevice = iDevice;
    }
}
