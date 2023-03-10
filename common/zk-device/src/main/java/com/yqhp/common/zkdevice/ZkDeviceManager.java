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
        String path = getPath(device.getLocation(), device.getId());
        zkTemplate.create(CreateMode.EPHEMERAL, path, toJSONString(device));
    }

    public void delete(ZkDevice device) {
        delete(device.getLocation(), device.getId());
    }

    public void delete(String location, String deviceId) {
        String path = getPath(location, deviceId);
        zkTemplate.delete(path);
    }

    public void update(ZkDevice device) {
        String path = getPath(device.getLocation(), device.getId());
        zkTemplate.setData(path, toJSONString(device));
    }

    public ZkDevice get(String location, String deviceId) {
        String path = getPath(location, deviceId);
        return get(path);
    }

    public ZkDevice get(String path) {
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

    private String getPath(String location, String deviceId) {
        return getWatchPath() + "/" + location + "/" + deviceId;
    }
}
