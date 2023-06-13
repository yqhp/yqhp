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
import com.yqhp.agent.androidtools.ADB;
import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.agent.devicediscovery.DeviceDiscovery;

import java.time.Duration;

/**
 * @author jiangyitao
 */
public class AndroidDeviceDiscovery extends DeviceDiscovery {

    private final String adbPath;
    private final Duration adbInitTimeout;

    public AndroidDeviceDiscovery(String adbPath, Duration adbInitTimeout) {
        this.adbPath = adbPath;
        this.adbInitTimeout = adbInitTimeout;
    }

    @Override
    protected void run(DeviceChangeListener listener) {
        ADB.addDeviceChangeListener((AndroidDebugBridge.IDeviceChangeListener) listener);
        ADB.init(adbPath, adbInitTimeout);
    }

    @Override
    protected void terminate() {
        ADB.terminate();
    }
}
