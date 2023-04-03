package com.yqhp.agent.web.controller;

import com.yqhp.agent.driver.DeviceInfo;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.file.model.OSSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
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

    @PostMapping("/jshell/eval")
    public List<JShellEvalResult> jshellEval(@RequestHeader("deviceToken") String token,
                                             @NotBlank(message = "input不能为空") String input) {
        return deviceService.jshellEval(token, input);
    }

    @PostMapping("/jshell/suggestions")
    public List<String> jshellCompletionSuggestions(@RequestHeader("deviceToken") String token,
                                                    @NotBlank(message = "input不能为空") String input) {
        return deviceService.jshellCompletionSuggestions(token, input);
    }

    @PostMapping("/jshell/docs")
    public List<String> jshellDocs(@RequestHeader("deviceToken") String token,
                                   @NotBlank(message = "input不能为空") String input) {
        return deviceService.jshellDocs(token, input);
    }
}
