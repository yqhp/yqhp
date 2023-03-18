package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePlanActionParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdatePlanActionParam;
import com.yqhp.console.repository.entity.PlanAction;

import java.util.List;

public interface PlanActionService extends IService<PlanAction> {

    PlanAction createPlanAction(CreatePlanActionParam param);

    List<PlanAction> createPlanActions(List<CreatePlanActionParam> params);

    PlanAction updatePlanAction(String id, UpdatePlanActionParam param);

    void deletePlanActionById(String id);

    PlanAction getPlanActionById(String id);

    List<String> listEnabledAndSortedActionIdByPlanId(String planId);

    List<PlanAction> listSortedByPlanId(String planId);

    void move(TableRowMoveEvent moveEvent);
}
