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
package com.yqhp.agent.devicediscovery;

/**
 * @author jiangyitao
 */
public abstract class DeviceDiscovery {

    private volatile boolean running = false;

    /**
     * 启动设备发现
     */
    public synchronized void start(DeviceChangeListener listener) {
        if (running) {
            throw new IllegalStateException("DeviceDiscovery is running");
        }

        run(listener);
        running = true;
    }

    /**
     * 停止设备发现
     */
    public synchronized boolean stop() {
        if (running) {
            terminate();
            running = false;
            return true;
        }
        return false;
    }

    protected abstract void run(DeviceChangeListener listener);

    protected abstract void terminate();
}
