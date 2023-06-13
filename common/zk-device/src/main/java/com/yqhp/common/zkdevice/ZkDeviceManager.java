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
package com.yqhp.common.zkdevice;

import com.yqhp.common.commons.util.JacksonUtils;
import com.yqhp.common.zookeeper.ZkTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.zookeeper.CreateMode;

/**
 * @author jiangyitao
 */
@Slf4j
public class ZkDeviceManager {

    private final ZkTemplate zkTemplate;

    public ZkDeviceManager(ZkTemplate zkTemplate) {
        this.zkTemplate = zkTemplate;
    }

    public String getWatchPath() {
        return "/yqhp/agent/device";
    }

    public void create(ZkDevice device) {
        String path = getDevicePath(device.getLocation(), device.getId());
        zkTemplate.create(CreateMode.EPHEMERAL, path, toJSONString(device));
    }

    public void delete(String location, String deviceId) {
        String path = getDevicePath(location, deviceId);
        zkTemplate.delete(path);
    }

    public void update(ZkDevice device) {
        String path = getDevicePath(device.getLocation(), device.getId());
        zkTemplate.setData(path, toJSONString(device));
    }

    public ZkDevice get(String location, String deviceId) {
        String path = getDevicePath(location, deviceId);
        return get(path);
    }

    public ZkDevice get(String path) {
        if (!zkTemplate.exists(path)) {
            return null;
        }
        // 如果path不存在，会抛异常
        byte[] data = zkTemplate.getData(path);
        return toZkDevice(data);
    }

    public ZkDevice toZkDevice(byte[] data) {
        if (ArrayUtils.isEmpty(data)) {
            return null;
        }
        return JacksonUtils.readValue(new String(data), ZkDevice.class);
    }

    private String toJSONString(ZkDevice device) {
        return JacksonUtils.writeValueAsString(device);
    }

    private String getDevicePath(String location, String deviceId) {
        return getWatchPath() + "/" + location + "/" + deviceId;
    }
}
