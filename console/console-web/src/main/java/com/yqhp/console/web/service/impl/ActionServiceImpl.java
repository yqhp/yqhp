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
import com.yqhp.console.repository.jsonfield.ActionX;
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

        Integer minWeight = getMinWeightByProjectId(param.getProjectId());
        action.setWeight(minWeight != null ? minWeight - 1 : null);

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
        Action action = getActionById(moveEvent.getFrom());
        if (ResourceFlags.unmovable(action.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.ACTION_UNMOVABLE);
        }

        // 移动到某个文件夹内
        if (moveEvent.isInner()) {
            action.setPkgId(moveEvent.getTo());
            update(action);
            return;
        }

        String currUid = CurrentUser.id();
        LocalDateTime now = LocalDateTime.now();
        Action toAction = getActionById(moveEvent.getTo());

        Action fromAction = new Action();
        fromAction.setId(action.getId());
        fromAction.setPkgId(toAction.getPkgId());
        fromAction.setWeight(toAction.getWeight());
        fromAction.setUpdateBy(currUid);
        fromAction.setUpdateTime(now);

        List<Action> toUpdateActions = new ArrayList<>();
        toUpdateActions.add(fromAction);
        toUpdateActions.addAll(
                listByProjectIdAndPkgIdAndWeightGeOrLe(
                        toAction.getProjectId(),
                        toAction.getPkgId(),
                        toAction.getWeight(),
                        moveEvent.isBefore()
                ).stream().map(a -> {
                    if (a.getId().equals(fromAction.getId())) {
                        return null;
                    }
                    Action toUpdate = new Action();
                    toUpdate.setId(a.getId());
                    toUpdate.setWeight(moveEvent.isBefore() ? a.getWeight() + 1 : a.getWeight() - 1);
                    toUpdate.setUpdateBy(currUid);
                    toUpdate.setUpdateTime(now);
                    return toUpdate;
                }).filter(Objects::nonNull).collect(Collectors.toList())
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
    public Action getActionById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.ACTION_NOT_FOUND));
    }

    @Override
    public ActionX getActionXById(String id,
                                  Map<String, ActionX> actionCache,
                                  Map<String, Doc> docCache) {
        if (actionCache.containsKey(id)) {
            return actionCache.get(id);
        }
        Action action = getById(id);
        ActionX actionX = toActionX(action, actionCache, docCache);
        actionCache.put(id, actionX);
        return actionX;
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
    public ActionX toActionX(Action action) {
        return toActionX(action, new HashMap<>(), new HashMap<>());
    }

    @Override
    public List<ActionX> listActionXByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        Map<String, ActionX> actionCache = new HashMap<>();
        Map<String, Doc> docCache = new HashMap<>();
        return ids.stream()
                .map(id -> getActionXById(id, actionCache, docCache))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ActionX toActionX(Action action,
                              Map<String, ActionX> actionCache,
                              Map<String, Doc> docCache) {
        if (action == null) return null;
        ActionX actionX = new ActionX();
        BeanUtils.copyProperties(action, actionX);
        actionX.setSteps(actionStepService.listActionStepXByActionId(actionX.getId(), actionCache, docCache));
        return actionX;
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

    private Integer getMinWeightByProjectId(String projectId) {
        return listByProjectId(projectId).stream()
                .min(Comparator.comparing(Action::getWeight))
                .map(Action::getWeight).orElse(null);
    }
}
