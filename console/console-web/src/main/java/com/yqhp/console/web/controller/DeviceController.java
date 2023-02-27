package com.yqhp.console.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yqhp.console.model.param.UpdateDeviceParam;
import com.yqhp.console.model.param.query.DevicePageQuery;
import com.yqhp.console.model.vo.DeviceVO;
import com.yqhp.console.repository.entity.Device;
import com.yqhp.console.web.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author jiangyitao
 */
@RequestMapping("/device")
@RestController
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/page")
    public IPage<DeviceVO> pageBy(DevicePageQuery query) {
        return deviceService.pageBy(query);
    }

    @GetMapping("/{deviceId}")
    public Device getDeviceById(@PathVariable String deviceId) {
        return deviceService.getDeviceById(deviceId);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{deviceId}")
    public void updateDevice(@PathVariable String deviceId, @Valid @RequestBody UpdateDeviceParam updateDeviceParam) {
        deviceService.updateDevice(deviceId, updateDeviceParam);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{deviceId}")
    public void deleteDeviceById(@PathVariable String deviceId) {
        deviceService.deleteDeviceById(deviceId);
    }
}
