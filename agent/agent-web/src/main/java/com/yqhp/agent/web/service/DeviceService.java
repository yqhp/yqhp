package com.yqhp.agent.web.service;

import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.driver.DeviceInfo;
import com.yqhp.file.model.OSSFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface DeviceService {
    void online(Device device);

    void offline(String deviceId);

    String lockDevice(String deviceId, String user);

    void unlockDevice(String token);

    boolean isDeviceLocked(String deviceId);

    List<DeviceDriver> getUnlockedDeviceDrivers();

    DeviceDriver getDeviceDriverById(String deviceId);

    DeviceDriver getDeviceDriverByToken(String token);

    void installAppByToken(String token, MultipartFile app);

    OSSFile screenshotByToken(String token, boolean isTmpFile);

    OSSFile screenshotById(String deviceId, boolean isTmpFile);

    DeviceInfo getDeviceInfo(String deviceId);
}
