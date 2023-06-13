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
package com.yqhp.agent.web.service;

import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.driver.DeviceInfo;
import com.yqhp.agent.driver.Hierarchy;
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

    Hierarchy dumpHierarchy(String token);

    OSSFile screenshotByToken(String token, boolean isTmpFile);

    OSSFile screenshotById(String deviceId, boolean isTmpFile);

    DeviceInfo getDeviceInfo(String deviceId);
}
