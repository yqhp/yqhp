package com.yqhp.agent.devicediscovery.android;

import com.android.ddmlib.IDevice;
import com.yqhp.agent.devicediscovery.Device;
import lombok.Getter;

/**
 * @author jiangyitao
 */
public class AndroidDevice extends Device {

    @Getter
    private final IDevice iDevice;

    AndroidDevice(IDevice iDevice) {
        super(iDevice.getSerialNumber(), iDevice.isEmulator());
        this.iDevice = iDevice;
    }

}
