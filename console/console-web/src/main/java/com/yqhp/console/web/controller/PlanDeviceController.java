package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreatePlanDeviceParam;
import com.yqhp.console.model.param.UpdatePlanDeviceParam;
import com.yqhp.console.repository.entity.PlanDevice;
import com.yqhp.console.web.service.PlanDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Validated
@RestController
@RequestMapping("/planDevice")
public class PlanDeviceController {

    @Autowired
    private PlanDeviceService planDeviceService;

    @PostMapping
    public void createPlanDevice(@Valid @RequestBody CreatePlanDeviceParam param) {
        planDeviceService.createPlanDevice(param);
    }

    @DeleteMapping("/{id}")
    public void deletePlanDeviceById(@PathVariable String id) {
        planDeviceService.deletePlanDeviceById(id);
    }

    @PutMapping("/{id}")
    public void updatePlanDevice(@PathVariable String id, @Valid @RequestBody UpdatePlanDeviceParam param) {
        planDeviceService.updatePlanDevice(id, param);
    }

    @GetMapping
    public List<PlanDevice> listByPlanId(@NotBlank(message = "planId不能为空") String planId) {
        return planDeviceService.listByPlanId(planId);
    }
}
