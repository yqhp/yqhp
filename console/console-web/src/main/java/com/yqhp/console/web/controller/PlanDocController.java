package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreatePlanDocParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdatePlanDocParam;
import com.yqhp.console.repository.entity.PlanDoc;
import com.yqhp.console.web.service.PlanDocService;
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
@RequestMapping("/planDoc")
public class PlanDocController {

    @Autowired
    private PlanDocService planDocService;

    @PostMapping
    public PlanDoc createPlanDoc(@Valid @RequestBody CreatePlanDocParam param) {
        return planDocService.createPlanDoc(param);
    }

    @PostMapping("/batch")
    public List<PlanDoc> createPlanDocs(@Valid @RequestBody List<CreatePlanDocParam> params) {
        return planDocService.createPlanDocs(params);
    }

    @DeleteMapping("/{id}")
    public void deletePlanDocById(@PathVariable String id) {
        planDocService.deletePlanDocById(id);
    }

    @PutMapping("/{id}")
    public PlanDoc updatePlanDoc(@PathVariable String id, @Valid @RequestBody UpdatePlanDocParam param) {
        return planDocService.updatePlanDoc(id, param);
    }

    @PutMapping("/move")
    public void move(@Valid @RequestBody TableRowMoveEvent moveEvent) {
        planDocService.move(moveEvent);
    }

    @GetMapping
    public List<PlanDoc> listSortedByPlanId(@NotBlank(message = "planId不能为空") String planId) {
        return planDocService.listSortedByPlanId(planId);
    }
}
