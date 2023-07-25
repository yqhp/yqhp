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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreatePlanParam;
import com.yqhp.console.model.param.UpdatePlanParam;
import com.yqhp.console.model.param.query.PlanPageQuery;
import com.yqhp.console.repository.entity.*;
import com.yqhp.console.repository.enums.DocKind;
import com.yqhp.console.repository.enums.ExecutionStatus;
import com.yqhp.console.repository.enums.RunMode;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import com.yqhp.console.repository.mapper.PlanMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {

    @Autowired
    private Snowflake snowflake;
    @Autowired
    private ExecutionRecordService executionRecordService;
    @Autowired
    private PluginExecutionRecordService pluginExecutionRecordService;
    @Autowired
    private DocExecutionRecordService docExecutionRecordService;
    @Autowired
    private DocService docService;
    @Autowired
    private PlanDeviceService planDeviceService;
    @Autowired
    private PlanDocService planDocService;
    @Autowired
    private PluginService pluginService;

    @Override
    public IPage<Plan> pageBy(PlanPageQuery query) {
        LambdaQueryWrapper<Plan> q = new LambdaQueryWrapper<>();
        q.eq(Plan::getProjectId, query.getProjectId());
        String keyword = query.getKeyword();
        q.and(StringUtils.hasText(keyword), c -> c
                .like(Plan::getId, keyword)
                .or()
                .like(Plan::getName, keyword)
        );
        q.orderByDesc(Plan::getId);
        return page(new Page<>(query.getPageNumb(), query.getPageSize()), q);
    }

    @Override
    public Plan createPlan(CreatePlanParam param) {
        Plan plan = param.convertTo();
        plan.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        plan.setCreateBy(currUid);
        plan.setUpdateBy(currUid);
        try {
            if (!save(plan)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PLAN_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN);
        }

        return getById(plan.getId());
    }

    @Override
    public Plan updatePlan(String id, UpdatePlanParam param) {
        Plan plan = getPlanById(id);
        param.update(plan);
        plan.setUpdateBy(CurrentUser.id());
        plan.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(plan)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLAN_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLAN);
        }
        return getById(id);
    }

    @Override
    public void deleteById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PLAN_FAIL);
        }
    }

    @Override
    public Plan getPlanById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PLAN_NOT_FOUND));
    }

    @Override
    public List<Plan> listByProjectId(String projectId) {
        Assert.hasText(projectId, "projectId must has text");
        LambdaQueryWrapper<Plan> query = new LambdaQueryWrapper<>();
        query.eq(Plan::getProjectId, projectId);
        return list(query);
    }

    @Transactional
    @Override
    public void exec(String id, String submitBy) {
        Plan plan = getPlanById(id);
        // 分配docs, deviceId->docs
        Map<String, List<Doc>> deviceDocsMap = assignDocs(plan);
        Set<String> deviceIds = deviceDocsMap.keySet();
        String createBy = StringUtils.hasText(submitBy) ? submitBy : plan.getCreateBy();

        // 保存计划执行记录
        ExecutionRecord executionRecord = new ExecutionRecord();
        executionRecord.setId(snowflake.nextIdStr());
        executionRecord.setProjectId(plan.getProjectId());
        executionRecord.setPlanId(plan.getId());
        executionRecord.setPlan(plan); // 保留提交执行时的计划
        executionRecord.setStatus(ExecutionStatus.TODO);
        executionRecord.setCreateBy(createBy);
        executionRecord.setUpdateBy(createBy);
        executionRecordService.save(executionRecord);

        // 保存设备plugin执行记录
        List<PluginDTO> plugins = pluginService.listDTOByProjectId(plan.getProjectId());
        if (!plugins.isEmpty()) {
            List<PluginExecutionRecord> pluginExecutionRecords = new ArrayList<>(deviceIds.size() * plugins.size());
            for (String deviceId : deviceIds) {
                pluginExecutionRecords.addAll(plugins.stream().map(plugin -> {
                    PluginExecutionRecord record = new PluginExecutionRecord();
                    record.setId(snowflake.nextIdStr());
                    record.setProjectId(plan.getProjectId());
                    record.setPlanId(plan.getId());
                    record.setExecutionRecordId(executionRecord.getId());
                    record.setDeviceId(deviceId);
                    record.setPluginId(plugin.getId());
                    record.setPlugin(plugin); // 保留提交执行时的plugin
                    record.setStatus(ExecutionStatus.TODO);
                    record.setCreateBy(createBy);
                    record.setUpdateBy(createBy);
                    return record;
                }).collect(Collectors.toList()));
            }
            pluginExecutionRecordService.saveBatch(pluginExecutionRecords);
        }

        // 保存设备doc执行记录
        List<DocExecutionRecord> docExecutionRecords = new ArrayList<>();
        deviceDocsMap.forEach((deviceId, docs) ->
                docExecutionRecords.addAll(docs.stream().map((doc) -> {
                    DocExecutionRecord record = new DocExecutionRecord();
                    record.setId(snowflake.nextIdStr());
                    record.setProjectId(plan.getProjectId());
                    record.setPlanId(plan.getId());
                    record.setExecutionRecordId(executionRecord.getId());
                    record.setDeviceId(deviceId);
                    record.setDocId(doc.getId());
                    record.setDocKind(doc.getKind());
                    record.setDoc(doc); // 保留提交执行时的doc
                    record.setStatus(ExecutionStatus.TODO);
                    record.setCreateBy(createBy);
                    record.setUpdateBy(createBy);
                    return record;
                }).collect(Collectors.toList()))
        );
        docExecutionRecordService.saveBatch(docExecutionRecords);

        for (String deviceId : deviceIds) {
            executionRecordService.push(deviceId, executionRecord.getId());
        }
    }

    /**
     * @return deviceId -> List<Doc>
     */
    private Map<String, List<Doc>> assignDocs(Plan plan) {
        List<String> deviceIds = planDeviceService.listEnabledAndSortedDeviceIdByPlanId(plan.getId());
        if (CollectionUtils.isEmpty(deviceIds)) {
            throw new ServiceException(ResponseCodeEnum.ENABLED_PLAN_DEVICES_NOT_FOUND);
        }
        List<String> docIds = planDocService.listEnabledAndSortedDocIdByPlanId(plan.getId());
        // listAvailableInIds返回的结果乱序，重新排一下
        List<Doc> planDocs = docService.listAvailableInIds(docIds).stream()
                .sorted(Comparator.comparingInt(doc -> docIds.indexOf(doc.getId())))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(planDocs)) {
            throw new ServiceException(ResponseCodeEnum.AVAILABLE_PLAN_DOCS_NOT_FOUND);
        }

        HashMap<String, List<Doc>> result = new HashMap<>();
        if (RunMode.COMPATIBLE.equals(plan.getRunMode())) {
            // 兼容模式，执行同一份
            for (String deviceId : deviceIds) {
                result.put(deviceId, planDocs);
            }
        } else if (RunMode.EFFICIENT.equals(plan.getRunMode())) {
            // 高效模式，平均分
            int i = 0;
            int deviceCount = deviceIds.size();
            for (Doc doc : planDocs) {
                if (i == deviceCount) i = 0;
                String deviceId = deviceIds.get(i);
                result.computeIfAbsent(deviceId, k -> new ArrayList<>()).add(doc);
                i++;
            }
        }

        List<Doc> initDocs = docService.scanPkgTree(plan.getProjectId(), DocKind.JSH_INIT);
        if (initDocs.isEmpty()) {
            return result;
        }
        // 高效模式下，有的设备可能分不到doc，以result.keySet()为准
        for (String deviceId : result.keySet()) {
            List<Doc> devicePlanDocs = result.get(deviceId);
            List<Doc> deviceAllDocs = new ArrayList<>(initDocs.size() + devicePlanDocs.size());
            deviceAllDocs.addAll(initDocs);
            deviceAllDocs.addAll(devicePlanDocs);
            result.put(deviceId, deviceAllDocs);
        }
        return result;
    }
}
