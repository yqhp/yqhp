package com.yqhp.console.web.controller;

import com.yqhp.auth.model.CurrentUser;
import com.yqhp.console.model.param.CreatePlanParam;
import com.yqhp.console.model.param.UpdatePlanParam;
import com.yqhp.console.repository.entity.Plan;
import com.yqhp.console.web.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    @GetMapping("/{id}")
    public Plan getPlanById(@PathVariable String id) {
        return planService.getPlanById(id);
    }

    @PostMapping
    public Plan createPlan(@RequestBody @Valid CreatePlanParam param) {
        return planService.createPlan(param);
    }

    @DeleteMapping("/{id}")
    public void deletePlanById(@PathVariable String id) {
        planService.deletePlanById(id);
    }

    @PutMapping("/{id}")
    public Plan updatePlan(@PathVariable String id, @Valid @RequestBody UpdatePlanParam param) {
        return planService.updatePlan(id, param);
    }

    @GetMapping("/{id}/exec")
    public void exec(@PathVariable String id) {
        planService.exec(id, CurrentUser.id());
    }
}
