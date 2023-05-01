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
import java.util.List;

/**
 * @author jiangyitao
 */
@RequestMapping("/device")
@RestController
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/all")
    public List<DeviceVO> listAllVO() {
        return deviceService.listAllVO();
    }

    @GetMapping("/page")
    public IPage<DeviceVO> pageVOBy(DevicePageQuery query) {
        return deviceService.pageVOBy(query);
    }

    @GetMapping("/{id}")
    public Device getDeviceById(@PathVariable String id) {
        return deviceService.getDeviceById(id);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}")
    public void updateDevice(@PathVariable String id, @Valid @RequestBody UpdateDeviceParam updateDeviceParam) {
        deviceService.updateDevice(id, updateDeviceParam);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        deviceService.deleteById(id);
    }
}
