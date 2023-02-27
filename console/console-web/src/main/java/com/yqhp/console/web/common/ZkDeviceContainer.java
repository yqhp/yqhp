package com.yqhp.console.web.common;

import com.yqhp.common.zkdevice.ZkDevice;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiangyitao
 */
public class ZkDeviceContainer {

    private static final ConcurrentHashMap<String, ZkDevice> ZK_DEVICES = new ConcurrentHashMap<>();

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
