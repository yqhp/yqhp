package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePlanDeviceParam;
import com.yqhp.console.model.param.UpdatePlanDeviceParam;
import com.yqhp.console.repository.entity.PlanDevice;

import java.util.List;

public interface PlanDeviceService extends IService<PlanDevice> {

    void createPlanDevice(CreatePlanDeviceParam param);

    void updatePlanDevice(String id, UpdatePlanDeviceParam param);

    void deletePlanDeviceById(String id);

    PlanDevice getPlanDeviceById(String id);

    List<String> listEnabledPlanDeviceIdByPlanId(String planId);
}
