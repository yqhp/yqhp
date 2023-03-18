package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateActionParam;
import com.yqhp.console.model.param.TreeNodeMoveEvent;
import com.yqhp.console.model.param.UpdateActionParam;
import com.yqhp.console.repository.entity.Action;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.mapper.ActionMapper;
import com.yqhp.console.web.common.ResourceFlags;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.ActionService;
import com.yqhp.console.web.service.ActionStepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
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

    @Autowired
    private Snowflake snowflake;
    @Lazy
    @Autowired
    private ActionStepService actionStepService;

    @Override
    public Action createAction(CreateActionParam param) {
        Action action = param.convertTo();
        action.setId(snowflake.nextIdStr());

        int minWeight = getMinWeightByProjectId(param.getProjectId());
        action.setWeight(minWeight - 1);

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
        param.update(action);
        update(action);
        return getById(id);
    }

    @Override
    @Transactional
    public void move(TreeNodeMoveEvent moveEvent) {
        Action from = getActionById(moveEvent.getFrom());
        if (ResourceFlags.unmovable(from.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.ACTION_UNMOVABLE);
        }

        // 移动到某个文件夹内
        if (moveEvent.isInner()) {
            from.setPkgId(moveEvent.getTo());
            update(from);
            return;
        }

        String currUid = CurrentUser.id();
        Action to = getActionById(moveEvent.getTo());

        Action fromAction = new Action();
        fromAction.setId(from.getId());
        fromAction.setPkgId(to.getPkgId());
        fromAction.setWeight(to.getWeight());
        fromAction.setUpdateBy(currUid);

        List<Action> toUpdateActions = new ArrayList<>();
        toUpdateActions.add(fromAction);
        toUpdateActions.addAll(
                listByProjectIdAndPkgIdAndWeightGeOrLe(
                        to.getProjectId(),
                        to.getPkgId(),
                        to.getWeight(),
                        moveEvent.isBefore()
                ).stream()
                        .filter(a -> !a.getId().equals(fromAction.getId()))
                        .map(a -> {
                            Action toUpdate = new Action();
                            toUpdate.setId(a.getId());
                            toUpdate.setWeight(moveEvent.isBefore() ? a.getWeight() + 1 : a.getWeight() - 1);
                            toUpdate.setUpdateBy(currUid);
                            return toUpdate;
                        }).collect(Collectors.toList())
        );

        try {
            if (!updateBatchById(toUpdateActions)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_ACTION_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ACTION);
        }
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
    public List<Action> listByProjectIdAndInPkgIds(String projectId, Collection<String> pkgIds) {
        if (CollectionUtils.isEmpty(pkgIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Action> query = new LambdaQueryWrapper<>();
        query.eq(Action::getProjectId, projectId);
        query.in(Action::getPkgId, pkgIds);
        return list(query);
    }

    @Override
    public Action getActionById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.ACTION_NOT_FOUND));
    }

    @Override
    public ActionDTO getActionDTOById(String id,
                                      Map<String, ActionDTO> actionCache,
                                      Map<String, Doc> docCache) {
        if (actionCache.containsKey(id)) {
            return actionCache.get(id);
        }
        Action action = getById(id);
        ActionDTO actionDTO = toActionDTO(action, actionCache, docCache);
        actionCache.put(id, actionDTO);
        return actionDTO;
    }

    @Override
    public ActionDTO toActionDTO(Action action) {
        return toActionDTO(action, new HashMap<>(), new HashMap<>());
    }

    @Override
    public List<ActionDTO> listActionDTOByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        Map<String, ActionDTO> actionCache = new HashMap<>();
        Map<String, Doc> docCache = new HashMap<>();
        return ids.stream()
                .map(id -> getActionDTOById(id, actionCache, docCache))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ActionDTO toActionDTO(Action action,
                                  Map<String, ActionDTO> actionCache,
                                  Map<String, Doc> docCache) {
        if (action == null) return null;
        ActionDTO actionDTO = new ActionDTO();
        BeanUtils.copyProperties(action, actionDTO);
        actionDTO.setSteps(actionStepService.listSortedActionStepDTOByActionId(actionDTO.getId(), actionCache, docCache));
        return actionDTO;
    }

    private List<Action> listByProjectIdAndPkgIdAndWeightGeOrLe(String projectId, String pkgId, Integer weight, boolean ge) {
        LambdaQueryWrapper<Action> query = new LambdaQueryWrapper<>();
        query.eq(Action::getProjectId, projectId)
                .eq(Action::getPkgId, pkgId);
        if (ge) {
            query.ge(Action::getWeight, weight);
        } else {
            query.le(Action::getWeight, weight);
        }
        return list(query);
    }

    private List<Action> listByProjectId(String projectId) {
        Assert.hasText(projectId, "projectId must has text");
        LambdaQueryWrapper<Action> query = new LambdaQueryWrapper<>();
        query.eq(Action::getProjectId, projectId);
        return list(query);
    }

    private int getMinWeightByProjectId(String projectId) {
        return listByProjectId(projectId).stream()
                .mapToInt(Action::getWeight)
                .min().orElse(1);
    }
}
