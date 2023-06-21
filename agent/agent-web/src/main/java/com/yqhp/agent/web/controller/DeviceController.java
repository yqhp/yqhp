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
package com.yqhp.agent.web.controller;

import com.yqhp.agent.androidtools.browser.Browser;
import com.yqhp.agent.driver.DeviceInfo;
import com.yqhp.agent.driver.Hierarchy;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.file.model.OSSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author jiangyitao
 */
@Validated
@RestController
@RequestMapping("/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/{deviceId}/lock")
    public String lock(@PathVariable String deviceId) {
        return deviceService.lockDevice(deviceId, CurrentUser.get().getNickname());
    }

    @GetMapping("/unlock")
    public void unlock(@RequestHeader("deviceToken") String token) {
        deviceService.unlockDevice(token);
    }

    @PostMapping("/installApp")
    public void installApp(@RequestHeader("deviceToken") String token,
                           @NotNull(message = "app不能为空") MultipartFile app) {
        deviceService.installAppByToken(token, app);
    }

    @GetMapping("/dumpHierarchy")
    public Hierarchy dumpHierarchy(@RequestHeader("deviceToken") String token) {
        return deviceService.dumpHierarchy(token);
    }

    @GetMapping("/browser")
    public List<Browser> listBrowser(@RequestHeader("deviceToken") String token) {
        return deviceService.listBrowser(token);
    }

    @GetMapping("/screenshot")
    public OSSFile screenshotByToken(@RequestHeader("deviceToken") String token) {
        return deviceService.screenshotByToken(token, true);
    }

    /**
     * admin可以不用deviceToken截图
     */
    @GetMapping("/{deviceId}/screenshot")
    @PreAuthorize("hasAuthority('admin')")
    public OSSFile screenshotById(@PathVariable String deviceId) {
        return deviceService.screenshotById(deviceId, false);
    }

    @GetMapping("/{deviceId}/info")
    public DeviceInfo getDeviceInfo(@PathVariable String deviceId) {
        return deviceService.getDeviceInfo(deviceId);
    }
}
