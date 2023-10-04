/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.*;
import com.yqhp.console.repository.entity.PlanDoc;
import com.yqhp.console.repository.mapper.PlanDocMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.PlanDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
                throw new ServiceException(ResponseCodeEnum.SAVE_PLAN_DOC_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DOC);
        }

        return getById(planDoc.getId());
    }

    @Override
    public List<PlanDoc> createPlanDocs(CreatePlanDocsParam param) {
        String planId = param.getPlanId();
        AtomicInteger maxWeight = new AtomicInteger(getMaxWeightByPlanId(planId));

        String currUid = CurrentUser.id();

        List<PlanDoc> planDocs = param.getDocIds().stream().map(docId -> {
            PlanDoc planDoc = new PlanDoc();
            planDoc.setId(snowflake.nextIdStr());
            planDoc.setPlanId(planId);
            planDoc.setDocId(docId);
            planDoc.setWeight(maxWeight.incrementAndGet());
            planDoc.setCreateBy(currUid);
            planDoc.setUpdateBy(currUid);
            return planDoc;
        }).collect(Collectors.toList());
        try {
            if (!saveBatch(planDocs)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PLAN_DOC_FAILED);
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
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLAN_DOC_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DOC);
        }
        return getById(id);
    }

    @Override
    public void deleteById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PLAN_DOC_FAILED);
        }
    }

    @Override
    public void deleteByPlanDoc(DeletePlanDocParam param) {
        LambdaQueryWrapper<PlanDoc> query = new LambdaQueryWrapper<>();
        query.eq(PlanDoc::getPlanId, param.getPlanId());
        query.eq(PlanDoc::getDocId, param.getDocId());
        if (!remove(query)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PLAN_DOC_FAILED);
        }
    }

    @Override
    public void deleteByDocId(String docId) {
        Assert.hasText(docId, "docId must has text");
        LambdaQueryWrapper<PlanDoc> query = new LambdaQueryWrapper<>();
        query.eq(PlanDoc::getDocId, docId);
        remove(query);
    }

    @Override
    public PlanDoc getPlanDocById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PLAN_DOC_NOT_FOUND));
    }

    @Override
    public List<String> listEnabledAndSortedDocIdByPlanId(String planId) {
        return listEnabledAndSortedByPlanId(planId).stream()
                .map(PlanDoc::getDocId).collect(Collectors.toList());
    }

    @Override
    public List<PlanDoc> listSortedByPlanId(String planId) {
        return listByPlanId(planId).stream()
                .sorted(Comparator.comparing(PlanDoc::getWeight))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanDoc> listByDocId(String docId) {
        Assert.hasText(docId, "docId must has text");
        LambdaQueryWrapper<PlanDoc> query = new LambdaQueryWrapper<>();
        query.eq(PlanDoc::getDocId, docId);
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
                        .filter(d -> !d.getId().equals(fromPlanDoc.getId()))
                        .map(d -> {
                            PlanDoc toUpdate = new PlanDoc();
                            toUpdate.setId(d.getId());
                            toUpdate.setWeight(moveEvent.isBefore() ? d.getWeight() + 1 : d.getWeight() - 1);
                            toUpdate.setUpdateBy(currUid);
                            return toUpdate;
                        }).collect(Collectors.toList())

        );
        try {
            if (!updateBatchById(toUpdatePlanDocs)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLAN_DOC_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN_DOC);
        }
    }

    private List<PlanDoc> listEnabledAndSortedByPlanId(String planId) {
        return listByPlanId(planId).stream()
                .filter(planDoc -> planDoc.getEnabled() == 1)
                .sorted(Comparator.comparing(PlanDoc::getWeight))
                .collect(Collectors.toList());
    }

    private List<PlanDoc> listByPlanId(String planId) {
        Assert.hasText(planId, "planId must has text");
        LambdaQueryWrapper<PlanDoc> query = new LambdaQueryWrapper<>();
        query.eq(PlanDoc::getPlanId, planId);
        return list(query);
    }

    private List<PlanDoc> listByPlanIdAndWeightGeOrLe(String planId, Integer weight, boolean ge) {
        List<PlanDoc> planDocs = listByPlanId(planId);
        return ge
                ? planDocs.stream().filter(planDoc -> planDoc.getWeight() >= weight).collect(Collectors.toList())
                : planDocs.stream().filter(planDoc -> planDoc.getWeight() <= weight).collect(Collectors.toList());
    }

    private int getMaxWeightByPlanId(String planId) {
        return listByPlanId(planId).stream()
                .mapToInt(PlanDoc::getWeight)
                .max().orElse(-1);
    }
}
