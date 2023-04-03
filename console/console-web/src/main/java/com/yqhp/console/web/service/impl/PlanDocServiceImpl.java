package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreatePlanDocParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdatePlanDocParam;
import com.yqhp.console.repository.entity.PlanDoc;
import com.yqhp.console.repository.mapper.PlanDocMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.PlanDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class PlanDocServiceImpl
        extends ServiceImpl<PlanDocMapper, PlanDoc>
        implements PlanDocService {

    @Autowired
    private Snowflake snowflake;

    @Override
    public PlanDoc createPlanDoc(CreatePlanDocParam param) {
        PlanDoc planDoc = param.convertTo();
        planDoc.setId(snowflake.nextIdStr());

        int maxWeight = getMaxWeightByPlanId(param.getPlanId());
        planDoc.setWeight(maxWeight + 1);

        String currUid = CurrentUser.id();
        planDoc.setCreateBy(currUid);
        planDoc.setUpdateBy(currUid);

        try {
            if (!save(planDoc)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PLAN_DOC_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DOC);
        }

        return getById(planDoc.getId());
    }

    @Override
    public List<PlanDoc> createPlanDocs(List<CreatePlanDocParam> params) {
        if (CollectionUtils.isEmpty(params)) return new ArrayList<>();
        String planId = params.get(0).getPlanId();
        AtomicInteger maxWeight = new AtomicInteger(getMaxWeightByPlanId(planId));

        String currUid = CurrentUser.id();

        List<PlanDoc> planDocs = params.stream().map(param -> {
            PlanDoc planDoc = param.convertTo();
            planDoc.setId(snowflake.nextIdStr());
            planDoc.setWeight(maxWeight.incrementAndGet());
            planDoc.setCreateBy(currUid);
            planDoc.setUpdateBy(currUid);
            return planDoc;
        }).collect(Collectors.toList());
        try {
            if (!saveBatch(planDocs)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PLAN_DOC_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DOC);
        }

        List<String> ids = planDocs.stream()
                .map(PlanDoc::getId).collect(Collectors.toList());
        return listByIds(ids);
    }

    @Override
    public PlanDoc updatePlanDoc(String id, UpdatePlanDocParam param) {
        PlanDoc planDoc = getPlanDocById(id);
        param.update(planDoc);
        planDoc.setUpdateBy(CurrentUser.id());
        planDoc.setUpdateTime(LocalDateTime.now());
        try {
            if (!updateById(planDoc)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLAN_DOC_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DOC);
        }
        return getById(id);
    }

    @Override
    public void deletePlanDocById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PLAN_DOC_FAIL);
        }
    }

    @Override
    public PlanDoc getPlanDocById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PLAN_DOC_NOT_FOUND));
    }

    @Override
    public List<String> listEnabledAndSortedDocIdByPlanId(String planId) {
        Assert.hasText(planId, "planId must has text");
        LambdaQueryWrapper<PlanDoc> query = new LambdaQueryWrapper<>();
        query.eq(PlanDoc::getPlanId, planId)
                .eq(PlanDoc::getEnabled, 1)
                .orderByAsc(PlanDoc::getWeight);
        return list(query).stream()
                .map(PlanDoc::getDocId)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanDoc> listSortedByPlanId(String planId) {
        Assert.hasText(planId, "planId must has text");
        LambdaQueryWrapper<PlanDoc> query = new LambdaQueryWrapper<>();
        query.eq(PlanDoc::getPlanId, planId);
        query.orderByAsc(PlanDoc::getWeight);
        return list(query);
    }

    @Override
    public void move(TableRowMoveEvent moveEvent) {
        PlanDoc from = getPlanDocById(moveEvent.getFrom());
        PlanDoc to = getPlanDocById(moveEvent.getTo());

        String currUid = CurrentUser.id();

        PlanDoc fromPlanDoc = new PlanDoc();
        fromPlanDoc.setId(from.getId());
        fromPlanDoc.setWeight(to.getWeight());
        fromPlanDoc.setUpdateBy(currUid);

        List<PlanDoc> toUpdatePlanDocs = new ArrayList<>();
        toUpdatePlanDocs.add(fromPlanDoc);
        toUpdatePlanDocs.addAll(
                listByPlanIdAndWeightGeOrLe(
                        to.getPlanId(),
                        to.getWeight(),
                        moveEvent.isBefore()
                ).stream()
                        .filter(a -> !a.getId().equals(fromPlanDoc.getId()))
                        .map(a -> {
                            PlanDoc toUpdate = new PlanDoc();
                            toUpdate.setId(a.getId());
                            toUpdate.setWeight(moveEvent.isBefore() ? a.getWeight() + 1 : a.getWeight() - 1);
                            toUpdate.setUpdateBy(currUid);
                            return toUpdate;
                        }).collect(Collectors.toList())

        );
        try {
            if (!updateBatchById(toUpdatePlanDocs)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLAN_DOC_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DOC);
        }
    }

    private List<PlanDoc> listByPlanIdAndWeightGeOrLe(String planId, Integer weight, boolean ge) {
        LambdaQueryWrapper<PlanDoc> query = new LambdaQueryWrapper<>();
        query.eq(PlanDoc::getPlanId, planId);
        if (ge) {
            query.ge(PlanDoc::getWeight, weight);
        } else {
            query.le(PlanDoc::getWeight, weight);
        }
        return list(query);
    }

    private int getMaxWeightByPlanId(String planId) {
        List<PlanDoc> planDocs = listSortedByPlanId(planId);
        PlanDoc maxWeightPlanDoc = CollectionUtils.lastElement(planDocs);
        return maxWeightPlanDoc == null ? -1 : maxWeightPlanDoc.getWeight();
    }
}
