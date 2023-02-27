package com.yqhp.console.web.controller;

import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.web.service.DeviceTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Validated
@RestController
@RequestMapping("/deviceTask")
public class DeviceTaskController {

    @Autowired
    private DeviceTaskService deviceTaskService;

    @GetMapping("/receive")
    public ReceivedDeviceTasks receive(@NotBlank(message = "设备id不能为空") String deviceId) {
        return deviceTaskService.receive(deviceId);
    }
}
