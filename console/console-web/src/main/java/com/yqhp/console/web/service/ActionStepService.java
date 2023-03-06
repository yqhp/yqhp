package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreateActionStepParam;
import com.yqhp.console.model.param.UpdateActionStepParam;
import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.jsonfield.ActionStepX;
import com.yqhp.console.repository.jsonfield.ActionX;

import java.util.List;
import java.util.Map;

public interface ActionStepService extends IService<ActionStep> {
    ActionStep createActionStep(CreateActionStepParam param);

    ActionStep updateActionStep(String id, UpdateActionStepParam param);

    ActionStep getActionStepById(String id);

    void deleteActionStepById(String id);

    List<ActionStepX> listActionStepXByActionId(String actionId,
                                                Map<String, ActionX> actionCache,
                                                Map<String, Doc> docCache);
}
