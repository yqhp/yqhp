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
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.entity.Plan;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import com.yqhp.console.repository.enums.DocKind;
import com.yqhp.console.repository.enums.PlanExecutionRecordStatus;
import com.yqhp.console.repository.enums.RunMode;
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
    private PlanExecutionRecordService planExecutionRecordService;
    @Autowired
    private DeviceTaskService deviceTaskService;
    @Autowired
    private PluginService pluginService;
    @Autowired
    private DocService docService;
    @Autowired
    private PlanDeviceService planDeviceService;
    @Autowired
    private PlanDocService planDocService;

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
    public void deletePlanById(String id) {
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
        // 分配设备任务
        Map<String, List<Doc>> deviceDocsMap = assignDeviceTasks(plan);
        String createBy = StringUtils.hasText(submitBy) ? submitBy : plan.getCreateBy();

        // 保存计划执行记录
        PlanExecutionRecord planExecutionRecord = new PlanExecutionRecord();
        planExecutionRecord.setId(snowflake.nextIdStr());
        planExecutionRecord.setProjectId(plan.getProjectId());
        planExecutionRecord.setPlanId(plan.getId());
        planExecutionRecord.setPlan(plan); // 保留提交执行时的计划
        planExecutionRecord.setPlugins(pluginService.listPluginDTOByProjectId(plan.getProjectId())); // 保留提交执行时的项目插件
        planExecutionRecord.setDocs(docService.listSortedDocByProjectIdAndKind(plan.getProjectId(), DocKind.JSH_INIT)); // 保留提交执行时的JSH_INIT
        planExecutionRecord.setStatus(PlanExecutionRecordStatus.UNCOMPLETED);
        planExecutionRecord.setCreateBy(createBy);
        planExecutionRecord.setUpdateBy(createBy);
        planExecutionRecordService.save(planExecutionRecord);

        // 保存设备任务
        List<DeviceTask> deviceTasks = new ArrayList<>();
        deviceDocsMap.forEach((deviceId, docs) ->
                deviceTasks.addAll(docs.stream().map((doc) -> {
                    DeviceTask task = new DeviceTask();
                    task.setId(snowflake.nextIdStr());
                    task.setProjectId(plan.getProjectId());
                    task.setPlanId(plan.getId());
                    task.setPlanExecutionRecordId(planExecutionRecord.getId());
                    task.setDeviceId(deviceId);
                    task.setDocId(doc.getId());
                    task.setDoc(doc); // 保留提交执行时的doc
                    task.setStatus(DeviceTaskStatus.TODO);
                    task.setCreateBy(createBy);
                    task.setUpdateBy(createBy);
                    return task;
                }).collect(Collectors.toList()))
        );
        deviceTaskService.saveBatch(deviceTasks);

        for (String deviceId : deviceDocsMap.keySet()) {
            deviceTaskService.cachePlanExecutionRecordForDevice(deviceId, planExecutionRecord.getId());
        }
    }

    /**
     * @return deviceId -> v
     */
    private Map<String, List<Doc>> assignDeviceTasks(Plan plan) {
        HashMap<String, List<Doc>> result = new HashMap<>();
        List<String> deviceIds = planDeviceService.listEnabledAndSortedPlanDeviceIdByPlanId(plan.getId());
        if (CollectionUtils.isEmpty(deviceIds)) {
            throw new ServiceException(ResponseCodeEnum.NO_DEVICES);
        }
        List<String> docIds = planDocService.listEnabledAndSortedDocIdByPlanId(plan.getId());
        // 查出来的有可能乱序，重新排一下
        List<Doc> docs = docService.listInIds(docIds).stream()
                .sorted(Comparator.comparingInt(doc -> docIds.indexOf(doc.getId())))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(docs)) {
            throw new ServiceException(ResponseCodeEnum.NO_DOCS);
        }

        if (RunMode.COMPATIBLE.equals(plan.getRunMode())) {
            // 兼容模式，执行同一份
            for (String deviceId : deviceIds) {
                result.put(deviceId, docs);
            }
        } else if (RunMode.EFFICIENT.equals(plan.getRunMode())) {
            // 高效模式，平均分
            int i = 0;
            int deviceCount = deviceIds.size();
            for (Doc doc : docs) {
                if (i == deviceCount) i = 0;
                String deviceId = deviceIds.get(i);
                result.computeIfAbsent(deviceId, k -> new ArrayList<>()).add(doc);
                i++;
            }
        }
        return result;
    }
}
