package com.yqhp.agent.web.service;

import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.driver.DeviceInfo;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.file.model.OSSFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface DeviceService {
    void online(Device device);

    void offline(String deviceId);

    List<DeviceDriver> getUnlockedDeviceDrivers();

    DeviceDriver getDeviceDriverById(String deviceId);

    void checkExists(String deviceId);

    String lockDevice(String deviceId, String user);

    boolean isDeviceLocked(String deviceId);

    DeviceDriver getDeviceDriverByToken(String token);

    void unlockDevice(String token);

    void installAppByToken(String token, MultipartFile app);

    OSSFile screenshotByToken(String token, boolean isTmpFile);

    OSSFile screenshotById(String deviceId, boolean isTmpFile);

    DeviceInfo getDeviceInfo(String deviceId);

    List<JShellEvalResult> jshellEval(String token, String input);

    List<String> jshellCompletionSuggestions(String token, String input);

    List<String> jshellDocs(String token, String input);

    void execAction(String token, ActionDTO action);
}
