/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
