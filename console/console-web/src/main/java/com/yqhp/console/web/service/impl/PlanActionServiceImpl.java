package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreatePlanActionParam;
import com.yqhp.console.model.param.UpdatePlanActionParam;
import com.yqhp.console.repository.entity.PlanAction;
import com.yqhp.console.repository.mapper.PlanActionMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.PlanActionService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanActionServiceImpl
        extends ServiceImpl<PlanActionMapper, PlanAction>
        implements PlanActionService {

    @Override
    public void createPlanAction(CreatePlanActionParam param) {
        PlanAction planAction = param.convertTo();

        Integer weight = getMaxWeightByPlanId(param.getPlanId());
        planAction.setWeight(weight != null ? weight + 1 : null);

        String currUid = CurrentUser.id();
        planAction.setCreateBy(currUid);
        planAction.setUpdateBy(currUid);

        try {
            if (!save(planAction)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PLAN_ACTION_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_ACTION);
        }
    }

    @Override
    public void updatePlanAction(String id, UpdatePlanActionParam param) {
        PlanAction planAction = getPlanActionById(id);
        param.update(planAction);
        planAction.setUpdateBy(CurrentUser.id());
        planAction.setUpdateTime(LocalDateTime.now());
        try {
            if (!updateById(planAction)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLAN_ACTION_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_ACTION);
        }
    }

    @Override
    public void deletePlanActionById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PLAN_ACTION_FAIL);
        }
    }

    @Override
    public PlanAction getPlanActionById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PLAN_ACTION_NOT_FOUND));
    }

    @Override
    public List<String> listEnabledActionIdByPlanId(String planId) {
        Assert.hasText(planId, "planId must has text");
        LambdaQueryWrapper<PlanAction> query = new LambdaQueryWrapper<>();
        query.eq(PlanAction::getPlanId, planId)
                .eq(PlanAction::getEnabled, 1);
        return list(query).stream()
                .map(PlanAction::getActionId)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanAction> listByPlanId(String planId) {
        Assert.hasText(planId, "planId must has text");
        LambdaQueryWrapper<PlanAction> query = new LambdaQueryWrapper<>();
        query.eq(PlanAction::getPlanId, planId);
        return list(query);
    }

    private Integer getMaxWeightByPlanId(String planId) {
        return listByPlanId(planId).stream()
                .max(Comparator.comparing(PlanAction::getWeight))
                .map(PlanAction::getWeight).orElse(null);
    }
}
