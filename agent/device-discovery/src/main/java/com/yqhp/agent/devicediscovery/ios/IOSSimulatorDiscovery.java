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

import java.time.Duration;

/**
 * @author jiangyitao
 */
public class IOSSimulatorDiscovery extends DeviceDiscovery {

    private final Duration scanPeriod;

    public IOSSimulatorDiscovery(Duration scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    @Override
    protected void run(DeviceChangeListener listener) {
        IOSSimulatorScheduledScanner.start(scanPeriod, listener);
    }

    @Override
    protected void terminate() {
        IOSSimulatorScheduledScanner.stop();
    }
}
