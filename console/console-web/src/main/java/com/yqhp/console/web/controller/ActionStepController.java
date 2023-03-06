package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreateActionStepParam;
import com.yqhp.console.model.param.UpdateActionStepParam;
import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.web.service.ActionStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/actionStep")
public class ActionStepController {

    @Autowired
    private ActionStepService actionStepService;

    @PostMapping
    public ActionStep createActionStep(@RequestBody @Valid CreateActionStepParam param) {
        return actionStepService.createActionStep(param);
    }

    @DeleteMapping("/{id}")
    public void deleteActionStepById(@PathVariable String id) {
        actionStepService.deleteActionStepById(id);
    }

    @PutMapping("/{id}")
    public ActionStep updateActionStep(@PathVariable String id, @Valid @RequestBody UpdateActionStepParam param) {
        return actionStepService.updateActionStep(id, param);
    }
}
