package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateActionParam;
import com.yqhp.console.model.param.UpdateActionParam;
import com.yqhp.console.repository.entity.Action;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.enums.ActionStatus;
import com.yqhp.console.repository.enums.ActionStepType;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStep;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;
import com.yqhp.console.repository.mapper.ActionMapper;
import com.yqhp.console.web.common.ResourceFlags;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.ActionService;
import com.yqhp.console.web.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class ActionServiceImpl extends ServiceImpl<ActionMapper, Action> implements ActionService {

    private static final List<ActionStatus> AVAILABLE_ACTION_STATUS_LIST = List.of(
            ActionStatus.RELEASED, ActionStatus.DEPRECATED
    );

    @Autowired
    private Snowflake snowflake;
    @Autowired
    private DocService docService;

    @Override
    public Action createAction(CreateActionParam param) {
        Action action = param.convertTo();
        action.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        action.setCreateBy(currUid);
        action.setUpdateBy(currUid);
        try {
            if (!save(action)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_ACTION_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ACTION);
        }

        return getById(action.getId());
    }

    @Override
    public Action updateAction(String id, UpdateActionParam param) {
        Action action = getActionById(id);
        if (ResourceFlags.unupdatable(action.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.ACTION_UNUPDATABLE);
        }
        boolean renamed = !action.getName().equals(param.getName());
        if (renamed && ResourceFlags.unrenamable(action.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.ACTION_UNRENAMABLE);
        }
        boolean moved = param.getPkgId() != null
                && !action.getPkgId().equals(param.getPkgId());
        if (moved && ResourceFlags.unmovable(action.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.ACTION_UNMOVABLE);
        }
        param.update(action);
        update(action);
        return getById(id);
    }

    @Override
    public void move(String id, String pkgId) {
        Action action = getActionById(id);
        boolean unmoved = action.getPkgId().equals(pkgId);
        if (unmoved) return;
        if (ResourceFlags.unmovable(action.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.ACTION_UNMOVABLE);
        }

        action.setPkgId(pkgId);
        update(action);
    }

    private void update(Action action) {
        action.setUpdateBy(CurrentUser.id());
        action.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(action)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_ACTION_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ACTION);
        }
    }

    @Override
    public void deleteActionById(String id) {
        Action action = getActionById(id);
        if (ResourceFlags.undeletable(action.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.ACTION_UNDELETABLE);
        }
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_ACTION_FAIL);
        }
    }

    @Override
    public Action getActionById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.ACTION_NOT_FOUND));
    }

    @Override
    public Action getAvailableActionById(String id) {
        LambdaQueryWrapper<Action> query = new LambdaQueryWrapper<>();
        query.eq(Action::getId, id);
        query.in(Action::getStatus, AVAILABLE_ACTION_STATUS_LIST);
        return getOne(query);
    }

    @Override
    public List<Action> listInPkgIds(Collection<String> pkgIds) {
        if (CollectionUtils.isEmpty(pkgIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Action> query = new LambdaQueryWrapper<>();
        query.in(Action::getPkgId, pkgIds);
        return list(query);
    }

    /**
     * @param action action未保存可能没id
     */
    @Override
    public ActionDTO toActionDTO(Action action) {
        return toActionDTO(action, new HashMap<>(), new HashMap<>());
    }

    @Override
    public List<ActionDTO> listAvailableActionDTOByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        Map<String, ActionDTO> availableActionCache = new HashMap<>();
        Map<String, Doc> availableDocCache = new HashMap<>();
        return ids.stream()
                .map(id -> getAvailableActionDTOById(id, availableActionCache, availableDocCache))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ActionDTO getAvailableActionDTOById(String id,
                                                Map<String, ActionDTO> availableActionCache,
                                                Map<String, Doc> availableDocCache) {
        if (availableActionCache.containsKey(id)) {
            return availableActionCache.get(id);
        }
        Action availableAction = getAvailableActionById(id);
        ActionDTO actionDTO = toActionDTO(availableAction, availableActionCache, availableDocCache);
        availableActionCache.put(id, actionDTO);
        return actionDTO;
    }

    private ActionDTO toActionDTO(Action action,
                                  Map<String, ActionDTO> availableActionCache,
                                  Map<String, Doc> availableDocCache) {
        if (action == null) return null;
        ActionDTO actionDTO = new ActionDTO();
        BeanUtils.copyProperties(action, actionDTO, "before", "steps", "after");
        actionDTO.setBefore(toEnabledActionStepDTOs(action.getBefore(), availableActionCache, availableDocCache));
        actionDTO.setSteps(toEnabledActionStepDTOs(action.getSteps(), availableActionCache, availableDocCache));
        actionDTO.setAfter(toEnabledActionStepDTOs(action.getAfter(), availableActionCache, availableDocCache));
        return actionDTO;
    }

    private List<ActionStepDTO> toEnabledActionStepDTOs(List<ActionStep> steps,
                                                        Map<String, ActionDTO> availableActionCache,
                                                        Map<String, Doc> availableDocCache) {
        if (CollectionUtils.isEmpty(steps)) {
            return new ArrayList<>();
        }
        return steps.stream()
                .filter(ActionStep::isEnabled)
                .map(step -> toActionStepDTO(step, availableActionCache, availableDocCache))
                .collect(Collectors.toList());
    }

    private ActionStepDTO toActionStepDTO(ActionStep actionStep,
                                          Map<String, ActionDTO> availableActionCache,
                                          Map<String, Doc> availableDocCache) {
        if (actionStep == null) return null;
        ActionStepDTO step = new ActionStepDTO();
        BeanUtils.copyProperties(actionStep, step);
        step.setExecutionId(snowflake.nextIdStr());

        String idOfType = step.getIdOfType();
        if (ActionStepType.ACTION.equals(actionStep.getType())) {
            ActionDTO availableAction = getAvailableActionDTOById(idOfType, availableActionCache, availableDocCache);
            step.setAction(availableAction);
        } else if (ActionStepType.DOC_JSHELL_RUN.equals(actionStep.getType())) {
            Doc availableDoc;
            if (availableDocCache.containsKey(idOfType)) {
                availableDoc = availableDocCache.get(idOfType);
            } else {
                availableDoc = docService.getAvailableDocById(idOfType);
                availableDocCache.put(idOfType, availableDoc);
            }
            step.setDoc(availableDoc);
        }
        return step;
    }
}
