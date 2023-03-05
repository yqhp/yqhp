package com.yqhp.console.web.controller;

import com.yqhp.console.model.TreeNodeMoveEvent;
import com.yqhp.console.model.param.CreateActionParam;
import com.yqhp.console.model.param.UpdateActionParam;
import com.yqhp.console.repository.entity.Action;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.web.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/action")
public class ActionController {

    @Autowired
    private ActionService actionService;

    @GetMapping("/{id}")
    public Action getActionById(@PathVariable String id) {
        return actionService.getActionById(id);
    }

    @PostMapping
    public Action createAction(@RequestBody @Valid CreateActionParam param) {
        return actionService.createAction(param);
    }

    @DeleteMapping("/{id}")
    public void deleteActionById(@PathVariable String id) {
        actionService.deleteActionById(id);
    }

    @PutMapping("/{id}")
    public Action updateAction(@PathVariable String id, @Valid @RequestBody UpdateActionParam param) {
        return actionService.updateAction(id, param);
    }

    @PutMapping("/move")
    public void move(@Valid @RequestBody TreeNodeMoveEvent moveEvent) {
        actionService.move(moveEvent);
    }

    @PostMapping("/dto")
    public ActionDTO toActionDTO(@RequestBody Action action) {
        return actionService.toActionDTO(action);
    }
}
