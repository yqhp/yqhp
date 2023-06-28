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
package com.yqhp.agent.web.service.impl;

import com.yqhp.agent.androidtools.browser.Browser;
import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.devicediscovery.android.AndroidDevice;
import com.yqhp.agent.devicediscovery.ios.IOSEmulator;
import com.yqhp.agent.devicediscovery.ios.IOSRealDevice;
import com.yqhp.agent.driver.*;
import com.yqhp.agent.web.enums.ResponseCodeEnum;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.common.web.util.MultipartFileUtils;
import com.yqhp.common.zkdevice.ZkDevice;
import com.yqhp.common.zkdevice.ZkDeviceManager;
import com.yqhp.common.zookeeper.ZkTemplate;
import com.yqhp.console.repository.enums.DevicePlatform;
import com.yqhp.console.repository.enums.DeviceType;
import com.yqhp.file.model.OSSFile;
import com.yqhp.file.rpc.FileRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService {

    /**
     * token : DeviceDriver
     */
    private static final ConcurrentHashMap<String, DeviceDriver> LOCKED_DEVICE_DRIVERS = new ConcurrentHashMap<>();

    /**
     * deviceId : DeviceDriver
     */
    private static final ConcurrentHashMap<String, DeviceDriver> DEVICE_DRIVERS = new ConcurrentHashMap<>();

    private final FileRpc fileRpc;
    private final ZkDeviceManager zkDeviceManager;
    private final String location;

    public DeviceServiceImpl(FileRpc fileRpc,
                             ZkTemplate zkTemplate,
                             ServiceInstance serviceInstance,
                             ServerProperties serverProperties) {
        this.fileRpc = fileRpc;
        this.zkDeviceManager = new ZkDeviceManager(zkTemplate);
//        String authority = serviceInstance.getUri().getAuthority(); // 192.168.31.247:-1，端口有问题
        String authority = serviceInstance.getHost() + ":" + serverProperties.getPort();
        location = Base64Utils.encodeToUrlSafeString(authority.getBytes());
        log.info("authority={} location={}", authority, location);
    }

    @Override
    public void online(Device device) {
        ZkDevice zkDevice;
        DeviceDriver deviceDriver;
        if (device instanceof AndroidDevice) {
            zkDevice = new ZkDevice(
                    device.getId(),
                    DevicePlatform.Android,
                    device.isEmulator() ? DeviceType.EMULATOR : DeviceType.REAL,
                    location
            );
            deviceDriver = new AndroidDeviceDriver((AndroidDevice) device);
        } else if (device instanceof IOSRealDevice) {
            zkDevice = new ZkDevice(
                    device.getId(),
                    DevicePlatform.iOS,
                    DeviceType.REAL,
                    location
            );
            deviceDriver = new IOSRealDeviceDriver((IOSRealDevice) device);
        } else if (device instanceof IOSEmulator) {
            zkDevice = new ZkDevice(
                    device.getId(),
                    DevicePlatform.iOS,
                    DeviceType.EMULATOR,
                    location
            );
            zkDevice.setModel(((IOSEmulator) device).getModel());
            deviceDriver = new IOSEmulatorDriver((IOSEmulator) device);
        } else {
            log.warn("unknown device={}", device);
            return;
        }

        zkDeviceManager.create(zkDevice);
        DEVICE_DRIVERS.put(device.getId(), deviceDriver);
    }

    @Override
    public void offline(String deviceId) {
        zkDeviceManager.delete(location, deviceId);
        DEVICE_DRIVERS.remove(deviceId);
    }

    @Override
    public String lockDevice(String deviceId, String user) {
        Assert.hasText(deviceId, "deviceId must has text");
        Assert.hasText(user, "user must has text");

        // 防止并发锁定同一个设备
        synchronized (deviceId.intern()) {
            if (isDeviceLocked(deviceId)) {
                throw new ServiceException(ResponseCodeEnum.DEVICE_LOCKED);
            }
            ZkDevice zkDevice = zkDeviceManager.get(location, deviceId);
            if (zkDevice == null) {
                throw new ServiceException(ResponseCodeEnum.DEVICE_NOT_FOUND);
            }
            zkDevice.setLocked(true);
            zkDevice.setLockUser(user);
            String token = createDeviceToken(deviceId);
            zkDevice.setLockToken(token);
            zkDevice.setLockTime(LocalDateTime.now());
            zkDeviceManager.update(zkDevice);
            LOCKED_DEVICE_DRIVERS.put(token, getDeviceDriverById(deviceId));
            log.info("[{}]locked by {}, token={}", deviceId, user, token);
            return token;
        }
    }

    private String createDeviceToken(String deviceId) {
        String data = System.currentTimeMillis() + RandomStringUtils.randomAlphanumeric(8) + deviceId;
        return DigestUtils.md5Hex(data);
    }

    @Override
    public void unlockDevice(String token) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);
        deviceDriver.release();
        ZkDevice zkDevice = zkDeviceManager.get(location, deviceDriver.getDeviceId());
        if (zkDevice != null) {
            zkDevice.setLocked(false);
            zkDevice.setUnlockTime(LocalDateTime.now());
            zkDeviceManager.update(zkDevice);
        }
        LOCKED_DEVICE_DRIVERS.remove(token);
        log.info("[{}]unlocked, token={}", deviceDriver.getDeviceId(), token);
    }

    @Override
    public boolean isDeviceLocked(String deviceId) {
        return LOCKED_DEVICE_DRIVERS.values().stream()
                .anyMatch(d -> d.getDeviceId().equals(deviceId));
    }

    @Override
    public List<DeviceDriver> getUnlockedDeviceDrivers() {
        Collection<DeviceDriver> allDrivers = DEVICE_DRIVERS.values();
        Collection<DeviceDriver> lockedDrivers = LOCKED_DEVICE_DRIVERS.values();
        return allDrivers.stream()
                .filter(driver -> !lockedDrivers.contains(driver))
                .collect(Collectors.toList());
    }

    @Override
    public DeviceDriver getDeviceDriverById(String deviceId) {
        return Optional.ofNullable(DEVICE_DRIVERS.get(deviceId))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.DEVICE_NOT_FOUND));
    }

    @Override
    public DeviceDriver getDeviceDriverByToken(String token) {
        return Optional.ofNullable(LOCKED_DEVICE_DRIVERS.get(token))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.INVALID_DEVICE_TOKEN));
    }

    @Override
    public void installAppByToken(String token, MultipartFile app) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);

        File appFile = null;
        try {
            appFile = MultipartFileUtils.toTempFile(app);
            deviceDriver.installApp(appFile);
        } catch (Exception e) {
            log.error("install {} err", app.getOriginalFilename(), e);
            throw new ServiceException(ResponseCodeEnum.INSTALL_APP_FAIL, e.getMessage());
        } finally {
            if (appFile != null && !appFile.delete()) {
                log.warn("delete {} fail", appFile);
            }
        }
    }

    @Override
    public Hierarchy dumpHierarchy(String token) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);
        return deviceDriver.dumpHierarchy();
    }

    @Override
    public List<Browser> listBrowser(String token) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);
        return ((AndroidDeviceDriver) deviceDriver).listBrowser();
    }

    @Override
    public OSSFile screenshotByToken(String token, boolean isTmpFile) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);
        return screenshot(deviceDriver, isTmpFile);
    }

    @Override
    public OSSFile screenshotById(String deviceId, boolean isTmpFile) {
        DeviceDriver deviceDriver = getDeviceDriverById(deviceId);
        return screenshot(deviceDriver, isTmpFile);
    }

    private OSSFile screenshot(DeviceDriver deviceDriver, boolean isTmpFile) {
        File img = null;
        try {
            img = deviceDriver.screenshot();
            MultipartFile multipartFile = MultipartFileUtils.toMultipartFile(img);
            return fileRpc.uploadFile(multipartFile, isTmpFile);
        } catch (Exception e) {
            log.error("screenshot err", e);
            throw new ServiceException(ResponseCodeEnum.SCREENSHOT_FAIL, e.getMessage());
        } finally {
            if (img != null && !img.delete()) {
                log.warn("delete {} fail", img);
            }
        }
    }

    @Override
    public DeviceInfo getDeviceInfo(String deviceId) {
        DeviceDriver deviceDriver = getDeviceDriverById(deviceId);
        return deviceDriver.getDeviceInfo();
    }

}
