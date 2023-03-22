package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePlanDeviceParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdatePlanDeviceParam;
import com.yqhp.console.repository.entity.PlanDevice;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface PlanDeviceService extends IService<PlanDevice> {

    PlanDevice createPlanDevice(CreatePlanDeviceParam param);

    List<PlanDevice> createPlanDevices(List<CreatePlanDeviceParam> params);

    PlanDevice updatePlanDevice(String id, UpdatePlanDeviceParam param);

    void deletePlanDeviceById(String id);

    PlanDevice getPlanDeviceById(String id);

    List<String> listEnabledAndSortedPlanDeviceIdByPlanId(String planId);

    List<PlanDevice> listSortedByPlanId(String planId);

    void move(TableRowMoveEvent moveEvent);
}
