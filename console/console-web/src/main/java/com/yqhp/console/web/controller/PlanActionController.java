package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreatePlanActionParam;
import com.yqhp.console.model.param.UpdatePlanActionParam;
import com.yqhp.console.repository.entity.PlanAction;
import com.yqhp.console.web.service.PlanActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Validated
@RestController
@RequestMapping("/planAction")
public class PlanActionController {

    @Autowired
    private PlanActionService planActionService;

    @PostMapping
    public void createPlanAction(@Valid @RequestBody CreatePlanActionParam param) {
        planActionService.createPlanAction(param);
    }

    @DeleteMapping("/{id}")
    public void deletePlanActionById(@PathVariable String id) {
        planActionService.deletePlanActionById(id);
    }

    @PutMapping("/{id}")
    public void updatePlanAction(@PathVariable String id, @Valid @RequestBody UpdatePlanActionParam param) {
        planActionService.updatePlanAction(id, param);
    }

    @GetMapping
    public List<PlanAction> listSortedByPlanId(@NotBlank(message = "planId不能为空") String planId) {
        return planActionService.listSortedByPlanId(planId);
    }
}
