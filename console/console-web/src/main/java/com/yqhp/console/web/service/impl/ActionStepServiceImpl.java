package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateActionStepParam;
import com.yqhp.console.model.param.UpdateActionStepParam;
import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.enums.ActionStepFlag;
import com.yqhp.console.repository.enums.ActionStepType;
import com.yqhp.console.repository.jsonfield.ActionStepX;
import com.yqhp.console.repository.jsonfield.ActionX;
import com.yqhp.console.repository.mapper.ActionStepMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.ActionService;
import com.yqhp.console.web.service.ActionStepService;
import com.yqhp.console.web.service.DocService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActionStepServiceImpl
        extends ServiceImpl<ActionStepMapper, ActionStep>
        implements ActionStepService {

    @Autowired
    private Snowflake snowflake;
    @Autowired
    private ActionService actionService;
    @Autowired
    private DocService docService;

    @Override
    public ActionStep createActionStep(CreateActionStepParam param) {
        ActionStep step = param.convertTo();
        step.setId(snowflake.nextIdStr());

        if (param.getWeight() == null) {
            Integer weight = getMaxWeightByActionIdAndFlag(param.getActionId(), param.getFlag());
            step.setWeight(weight != null ? weight + 1 : null);
        }

        String currUid = CurrentUser.id();
        step.setCreateBy(currUid);
        step.setUpdateBy(currUid);
        try {
            if (!save(step)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_ACTION_STEP_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ACTION_STEP);
        }
        return getById(step.getId());
    }

    @Override
    public ActionStep updateActionStep(String id, UpdateActionStepParam param) {
        ActionStep step = getActionStepById(id);
        param.update(step);
        step.setUpdateBy(CurrentUser.id());
        step.setUpdateTime(LocalDateTime.now());
        try {
            if (!updateById(step)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_ACTION_STEP_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ACTION_STEP);
        }
        return getById(id);
    }

    @Override
    public ActionStep getActionStepById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.ACTION_STEP_NOT_FOUND));
    }

    @Override
    public void deleteActionStepById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_ACTION_STEP_FAIL);
        }
    }

    @Override
    public List<ActionStepX> listActionStepXByActionId(String actionId,
                                                       Map<String, ActionX> actionCache,
                                                       Map<String, Doc> docCache) {

        List<ActionStep> steps = listByActionId(actionId);
        return toActionStepXs(steps, actionCache, docCache);
    }

    private List<ActionStep> listByActionId(String actionId) {
        Assert.hasText(actionId, "actionId must has text");
        LambdaQueryWrapper<ActionStep> query = new LambdaQueryWrapper<>();
        query.eq(ActionStep::getActionId, actionId);
        return list(query);
    }

    private List<ActionStepX> toActionStepXs(List<ActionStep> steps,
                                             Map<String, ActionX> actionCache,
                                             Map<String, Doc> docCache) {
        if (CollectionUtils.isEmpty(steps)) {
            return new ArrayList<>();
        }
        return steps.stream()
                .map(step -> toActionStepX(step, actionCache, docCache))
                .collect(Collectors.toList());
    }


    private ActionStepX toActionStepX(ActionStep step,
                                      Map<String, ActionX> actionCache,
                                      Map<String, Doc> docCache) {
        if (step == null) return null;
        ActionStepX stepX = new ActionStepX();
        BeanUtils.copyProperties(step, stepX);
        stepX.setExecutionId(snowflake.nextIdStr());

        String idOfType = stepX.getIdOfType();
        if (ActionStepType.ACTION.equals(stepX.getType())) {
            ActionX actionX = actionService.getActionXById(idOfType, actionCache, docCache);
            stepX.setAction(actionX);
        } else if (ActionStepType.DOC_JSHELL_RUN.equals(stepX.getType())) {
            Doc doc;
            if (docCache.containsKey(idOfType)) {
                doc = docCache.get(idOfType);
            } else {
                doc = docService.getById(idOfType);
                docCache.put(idOfType, doc);
            }
            stepX.setDoc(doc);
        }
        return stepX;
    }

    private List<ActionStep> listByActionIdAndFlag(String actionId, ActionStepFlag flag) {
        Assert.hasText(actionId, "actionId must has text");
        Assert.notNull(flag, "flag cannot be null");
        LambdaQueryWrapper<ActionStep> query = new LambdaQueryWrapper<>();
        query.eq(ActionStep::getActionId, actionId);
        query.eq(ActionStep::getFlag, flag);
        return list(query);
    }

    private Integer getMaxWeightByActionIdAndFlag(String actionId, ActionStepFlag flag) {
        return listByActionIdAndFlag(actionId, flag).stream()
                .max(Comparator.comparing(ActionStep::getWeight))
                .map(ActionStep::getWeight).orElse(null);
    }
}
