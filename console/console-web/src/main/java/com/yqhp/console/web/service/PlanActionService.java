package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePlanActionParam;
import com.yqhp.console.model.param.UpdatePlanActionParam;
import com.yqhp.console.repository.entity.PlanAction;

import java.util.List;

public interface PlanActionService extends IService<PlanAction> {

    void createPlanAction(CreatePlanActionParam param);

    void updatePlanAction(String id, UpdatePlanActionParam param);

    void deletePlanActionById(String id);

    PlanAction getPlanActionById(String id);

    List<String> listEnabledAndSortedActionIdByPlanId(String planId);

    List<PlanAction> listSortedByPlanId(String planId);
}
