package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePlanParam;
import com.yqhp.console.model.param.UpdatePlanParam;
import com.yqhp.console.repository.entity.Plan;

/**
 * @author jiangyitao
 */
public interface PlanService extends IService<Plan> {

    Plan createPlan(CreatePlanParam param);

    Plan updatePlan(String id, UpdatePlanParam param);

    void deletePlanById(String id);

    Plan getPlanById(String id);

    void exec(String id, String submitBy);
}
