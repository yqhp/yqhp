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
package com.yqhp.agent.devicediscovery.ios;

import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.agent.devicediscovery.DeviceDiscovery;
import com.yqhp.agent.iostools.usbmuxd.IDeviceChangeListener;
import com.yqhp.agent.iostools.usbmuxd.Usbmuxd;

/**
 * @author jiangyitao
 */
public class IOSRealDeviceDiscovery extends DeviceDiscovery {

    private final Usbmuxd usbmuxd = new Usbmuxd();

    @Override
    protected void run(DeviceChangeListener listener) {
        usbmuxd.startListenDevices((IDeviceChangeListener) listener);
    }

    @Override
    protected void terminate() {
        usbmuxd.stopListenDevices();
    }
}
