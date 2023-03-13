package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateActionStepParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdateActionStepParam;
import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.enums.ActionStepKind;
import com.yqhp.console.repository.enums.ActionStepType;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;
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

        Integer weight = getMaxWeightByActionIdAndKind(param.getActionId(), param.getKind());
        step.setWeight(weight != null ? weight + 1 : null);

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
    public List<ActionStep> listByActionId(String actionId) {
        Assert.hasText(actionId, "actionId must has text");
        LambdaQueryWrapper<ActionStep> query = new LambdaQueryWrapper<>();
        query.eq(ActionStep::getActionId, actionId);
        return list(query);
    }

    @Override
    public void move(TableRowMoveEvent moveEvent) {
        ActionStep step = getActionStepById(moveEvent.getFrom());
        ActionStep toStep = getActionStepById(moveEvent.getTo());

        String currUid = CurrentUser.id();
        LocalDateTime now = LocalDateTime.now();

        ActionStep fromStep = new ActionStep();
        fromStep.setId(step.getId());
        fromStep.setWeight(toStep.getWeight());
        fromStep.setUpdateBy(currUid);
        fromStep.setUpdateTime(now);

        List<ActionStep> toUpdateSteps = new ArrayList<>();
        toUpdateSteps.add(fromStep);
        toUpdateSteps.addAll(
                listByActionIdAndKindAndWeightGeOrLe(
                        toStep.getActionId(),
                        toStep.getKind(),
                        toStep.getWeight(),
                        moveEvent.isBefore()
                ).stream().map(s -> {
                    if (s.getId().equals(fromStep.getId())) {
                        return null;
                    }
                    ActionStep toUpdate = new ActionStep();
                    toUpdate.setId(s.getId());
                    toUpdate.setWeight(moveEvent.isBefore() ? s.getWeight() + 1 : s.getWeight() - 1);
                    toUpdate.setUpdateBy(currUid);
                    toUpdate.setUpdateTime(now);
                    return toUpdate;
                }).filter(Objects::nonNull).collect(Collectors.toList())
        );

        try {
            if (!updateBatchById(toUpdateSteps)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_ACTION_STEP_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ACTION_STEP);
        }
    }

    @Override
    public List<ActionStepDTO> listActionStepDTOByActionId(String actionId,
                                                           Map<String, ActionDTO> actionCache,
                                                           Map<String, Doc> docCache) {

        List<ActionStep> steps = listByActionId(actionId);
        return toActionStepDTOs(steps, actionCache, docCache);
    }

    private List<ActionStepDTO> toActionStepDTOs(List<ActionStep> steps,
                                                 Map<String, ActionDTO> actionCache,
                                                 Map<String, Doc> docCache) {
        if (CollectionUtils.isEmpty(steps)) {
            return new ArrayList<>();
        }
        return steps.stream()
                .map(step -> toActionStepDTO(step, actionCache, docCache))
                .collect(Collectors.toList());
    }


    private ActionStepDTO toActionStepDTO(ActionStep step,
                                          Map<String, ActionDTO> actionCache,
                                          Map<String, Doc> docCache) {
        if (step == null) return null;
        ActionStepDTO stepDTO = new ActionStepDTO();
        BeanUtils.copyProperties(step, stepDTO);

        String idOfType = stepDTO.getIdOfType();
        if (ActionStepType.ACTION.equals(stepDTO.getType())) {
            ActionDTO actionDTO = actionService.getActionDTOById(idOfType, actionCache, docCache);
            stepDTO.setAction(actionDTO);
        } else if (ActionStepType.DOC_JSH_EXECUTABLE.equals(stepDTO.getType())) {
            Doc doc;
            if (docCache.containsKey(idOfType)) {
                doc = docCache.get(idOfType);
            } else {
                doc = docService.getById(idOfType);
                docCache.put(idOfType, doc);
            }
            stepDTO.setDoc(doc);
        }
        return stepDTO;
    }

    private List<ActionStep> listByActionIdAndKindAndWeightGeOrLe(String actionId, ActionStepKind kind, Integer weight, boolean ge) {
        LambdaQueryWrapper<ActionStep> query = new LambdaQueryWrapper<>();
        query.eq(ActionStep::getActionId, actionId);
        query.eq(ActionStep::getKind, kind);
        if (ge) {
            query.ge(ActionStep::getWeight, weight);
        } else {
            query.le(ActionStep::getWeight, weight);
        }
        return list(query);
    }

    private List<ActionStep> listByActionIdAndKind(String actionId, ActionStepKind kind) {
        Assert.hasText(actionId, "actionId must has text");
        Assert.notNull(kind, "kind cannot be null");
        LambdaQueryWrapper<ActionStep> query = new LambdaQueryWrapper<>();
        query.eq(ActionStep::getActionId, actionId);
        query.eq(ActionStep::getKind, kind);
        return list(query);
    }

    private Integer getMaxWeightByActionIdAndKind(String actionId, ActionStepKind kind) {
        return listByActionIdAndKind(actionId, kind).stream()
                .max(Comparator.comparing(ActionStep::getWeight))
                .map(ActionStep::getWeight).orElse(null);
    }
}
