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
package com.yqhp.console.web.common;

import com.yqhp.common.zkdevice.ZkDevice;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiangyitao
 */
public class ZkDeviceContainer {

    private static final Map<String, ZkDevice> ZK_DEVICES = new ConcurrentHashMap<>();

    public static void add(ZkDevice zkDevice) {
        ZK_DEVICES.put(zkDevice.getId(), zkDevice);
    }

    public static void removeById(String deviceId) {
        ZK_DEVICES.remove(deviceId);
    }

    public static ZkDevice getById(String deviceId) {
        return ZK_DEVICES.get(deviceId);
    }

    public static Set<String> getAllDeviceIds() {
        return ZK_DEVICES.keySet();
    }

    public static Collection<ZkDevice> getAll() {
        return ZK_DEVICES.values();
    }

}
