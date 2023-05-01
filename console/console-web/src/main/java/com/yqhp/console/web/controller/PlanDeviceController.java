package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreatePlanDeviceParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdatePlanDeviceParam;
import com.yqhp.console.model.vo.PlanDeviceVO;
import com.yqhp.console.repository.entity.PlanDevice;
import com.yqhp.console.web.service.PlanDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author jiangyitao
 */
@Validated
@RestController
@RequestMapping("/planDevice")
public class PlanDeviceController {

    @Autowired
    private PlanDeviceService planDeviceService;

    @PostMapping
    public PlanDevice createPlanDevice(@Valid @RequestBody CreatePlanDeviceParam param) {
        return planDeviceService.createPlanDevice(param);
    }

    @PostMapping("/batch")
    public List<PlanDevice> createPlanDevices(@Valid @RequestBody List<CreatePlanDeviceParam> params) {
        return planDeviceService.createPlanDevices(params);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        planDeviceService.deleteById(id);
    }

    @PutMapping("/{id}")
    public PlanDevice updatePlanDevice(@PathVariable String id, @Valid @RequestBody UpdatePlanDeviceParam param) {
        return planDeviceService.updatePlanDevice(id, param);
    }

    @PutMapping("/move")
    public void move(@Valid @RequestBody TableRowMoveEvent moveEvent) {
        planDeviceService.move(moveEvent);
    }

    @GetMapping
    public List<PlanDeviceVO> listSortedVOByPlanId(@NotBlank(message = "planId不能为空") String planId) {
        return planDeviceService.listSortedVOByPlanId(planId);
    }
}
