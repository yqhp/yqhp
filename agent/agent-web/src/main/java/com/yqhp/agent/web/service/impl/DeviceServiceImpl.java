package com.yqhp.agent.web.service.impl;

import com.yqhp.agent.action.ActionExecutor;
import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.devicediscovery.android.AndroidDevice;
import com.yqhp.agent.devicediscovery.ios.IOSEmulator;
import com.yqhp.agent.devicediscovery.ios.IOSRealDevice;
import com.yqhp.agent.driver.*;
import com.yqhp.agent.web.enums.ResponseCodeEnum;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.common.web.util.MultipartFileUtils;
import com.yqhp.common.zkdevice.ZkDevice;
import com.yqhp.common.zkdevice.ZkDeviceManager;
import com.yqhp.common.zookeeper.ZkTemplate;
import com.yqhp.console.repository.enums.DevicePlatform;
import com.yqhp.console.repository.enums.DeviceType;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.file.model.OSSFile;
import com.yqhp.file.rpc.FileRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;
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
//        String authority = serviceInstance.getUri().getAuthority(); // 192.168.31.247:-1??????????????????
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

        DEVICE_DRIVERS.put(device.getId(), deviceDriver);
        zkDeviceManager.create(zkDevice);
    }

    @Override
    public void offline(String deviceId) {
        DeviceDriver removedDeviceDriver = DEVICE_DRIVERS.remove(deviceId);
        LOCKED_DEVICE_DRIVERS.values().remove(removedDeviceDriver);
        zkDeviceManager.delete(location, deviceId);
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
    public void checkExists(String deviceId) {
        getDeviceDriverById(deviceId);
    }

    @Override
    public String lockDevice(String deviceId, String user) {
        Assert.hasText(deviceId, "deviceId must has text");
        Assert.hasText(user, "user must has text");

        // ?????????????????????????????????
        synchronized (deviceId.intern()) {
            if (isDeviceLocked(deviceId)) {
                throw new ServiceException(ResponseCodeEnum.DEVICE_LOCKED);
            }

            String token = createDeviceToken(deviceId);
            lock(deviceId, user, token);
            log.info("[{}]locked by {}, token={}", deviceId, user, token);
            return token;
        }
    }

    @Override
    public boolean isDeviceLocked(String deviceId) {
        checkExists(deviceId);
        return LOCKED_DEVICE_DRIVERS.values().stream()
                .anyMatch(d -> d.getDeviceId().equals(deviceId));
    }

    @Override
    public DeviceDriver getDeviceDriverByToken(String token) {
        return Optional.ofNullable(LOCKED_DEVICE_DRIVERS.get(token))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.INVALID_DEVICE_TOKEN));
    }

    @Override
    public void unlockDevice(String token) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);
        deviceDriver.release();
        unlock(deviceDriver.getDeviceId(), token);
        log.info("[{}]unlocked, token={}", deviceDriver.getDeviceId(), token);
    }

    @Override
    public void installAppByToken(String token, MultipartFile app) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);

        File apk = null;
        try {
            apk = MultipartFileUtils.toTempFile(app);
            deviceDriver.installApp(apk);
        } catch (Exception e) {
            log.error("install {} err", app.getOriginalFilename(), e);
            throw new ServiceException(ResponseCodeEnum.INSTALL_APP_FAIL, e.getMessage());
        } finally {
            if (apk != null && !apk.delete()) {
                log.warn("delete {} fail", apk);
            }
        }
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

    @Override
    public List<JShellEvalResult> jshellEval(String token, String input) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);
        return deviceDriver.jshellEval(input);
    }

    @Override
    public List<String> jshellCompletionSuggestions(String token, String input) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);
        return deviceDriver.jshellCompletionSuggestions(input);
    }

    @Override
    public List<String> jshellDocs(String token, String input) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);
        return deviceDriver.jshellDocs(input);
    }

    @Override
    public void execAction(String token, ActionDTO action) {
        DeviceDriver deviceDriver = getDeviceDriverByToken(token);
        // todo ????????????
        new ActionExecutor(deviceDriver).exec(action);
    }

    private String createDeviceToken(String deviceId) {
        String data = System.currentTimeMillis() + RandomStringUtils.randomAlphanumeric(8) + deviceId;
        return DigestUtils.md5DigestAsHex(data.getBytes());
    }

    private void lock(String deviceId, String lockUser, String lockToken) {
        ZkDevice zkDevice = zkDeviceManager.get(location, deviceId);
        zkDevice.setLocked(true);
        zkDevice.setLockUser(lockUser);
        zkDevice.setLockToken(lockToken);
        zkDevice.setLockTime(LocalDateTime.now());
        zkDeviceManager.update(zkDevice);
        LOCKED_DEVICE_DRIVERS.put(lockToken, getDeviceDriverById(deviceId));
    }

    private void unlock(String deviceId, String lockToken) {
        ZkDevice zkDevice = zkDeviceManager.get(location, deviceId);
        zkDevice.setLocked(false);
        zkDevice.setUnlockTime(LocalDateTime.now());
        zkDeviceManager.update(zkDevice);
        LOCKED_DEVICE_DRIVERS.remove(lockToken);
    }
}
