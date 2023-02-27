package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreatePlanParam;
import com.yqhp.console.model.param.UpdatePlanParam;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.Plan;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.repository.entity.StepExecutionRecord;
import com.yqhp.console.repository.enums.*;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;
import com.yqhp.console.repository.jsonfield.PlanAction;
import com.yqhp.console.repository.jsonfield.PlanDevice;
import com.yqhp.console.repository.mapper.PlanMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private ActionService actionService;
    @Autowired
    private DeviceTaskService deviceTaskService;
    @Autowired
    private StepExecutionRecordService stepExecutionRecordService;
    @Autowired
    private PluginService pluginService;
    @Autowired
    private DocService docService;

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

    @Transactional
    @Override
    public void exec(String id, String submitBy) {
        Plan plan = getPlanById(id);
        Map<String, List<ActionDTO>> deviceActionsMap = assignDeviceTasks(plan);
        if (deviceActionsMap.isEmpty()) {
            throw new ServiceException(ResponseCodeEnum.NO_DEVICES_OR_ACTIONS);
        }

        String createBy = StringUtils.hasText(submitBy) ? submitBy : plan.getCreateBy();

        // 保存计划执行记录
        PlanExecutionRecord planExecutionRecord = new PlanExecutionRecord();
        planExecutionRecord.setId(snowflake.nextIdStr());
        planExecutionRecord.setProjectId(plan.getProjectId());
        planExecutionRecord.setPlanId(plan.getId());
        planExecutionRecord.setPlanName(plan.getName()); // 保留提交执行时的计划名
        planExecutionRecord.setPlugins(pluginService.listByProjectId(plan.getProjectId())); // 保留提交执行时的项目插件
        planExecutionRecord.setDocs(docService.listAvailableDocByProjectIdAndType(plan.getProjectId(), DocType.JAVA)); // 保留提交执行时的可用java代码
        planExecutionRecord.setStatus(PlanExecutionRecordStatus.UNCOMPLETED);
        planExecutionRecord.setCreateBy(createBy);
        planExecutionRecord.setUpdateBy(createBy);
        planExecutionRecordService.save(planExecutionRecord);

        // 保存设备任务和步骤执行记录
        List<DeviceTask> deviceTasks = new ArrayList<>();
        List<StepExecutionRecord> stepExecutionRecords = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        deviceActionsMap.forEach((deviceId, actions) -> {
            for (ActionDTO action : actions) {
                DeviceTask task = new DeviceTask();
                task.setId(snowflake.nextIdStr());
                task.setProjectId(plan.getProjectId());
                task.setPlanId(plan.getId());
                task.setPlanExecutionRecordId(planExecutionRecord.getId());
                task.setDeviceId(deviceId);
                task.setActionId(action.getId());
                task.setAction(action);
                task.setStatus(DeviceTaskStatus.TODO);
                task.setCreateBy(createBy);
                task.setCreateTime(now);
                task.setUpdateBy(createBy);
                task.setUpdateTime(now);
                deviceTasks.add(task);

                addStepExecutionRecords(stepExecutionRecords, task, action, ActionStepsType.BEFORE, action.getBefore());
                addStepExecutionRecords(stepExecutionRecords, task, action, ActionStepsType.STEPS, action.getSteps());
                addStepExecutionRecords(stepExecutionRecords, task, action, ActionStepsType.AFTER, action.getAfter());
            }
        });
        if (stepExecutionRecords.isEmpty()) {
            throw new ServiceException(ResponseCodeEnum.NO_STEP_EXECUTION_RECORDS);
        }
        deviceTaskService.saveBatch(deviceTasks);
        stepExecutionRecordService.saveBatch(stepExecutionRecords);

        for (String deviceId : deviceActionsMap.keySet()) {
            deviceTaskService.cachePlanExecutionRecordIdForDevice(deviceId, planExecutionRecord.getId());
        }
    }

    private void addStepExecutionRecords(List<StepExecutionRecord> stepExecutionRecords, DeviceTask task,
                                         ActionDTO action, ActionStepsType stepsType, List<ActionStepDTO> steps) {
        if (CollectionUtils.isEmpty(steps)) return;
        stepExecutionRecords.addAll(steps.stream().map(step -> {
            StepExecutionRecord record = new StepExecutionRecord();
            record.setId(step.getExecutionId());
            record.setDeviceTaskId(task.getId());
            record.setDeviceId(task.getDeviceId());
            record.setActionId(action.getId());
            record.setActionStepsType(stepsType);
            record.setStepName(step.getName());
            record.setStatus(StepExecutionStatus.TODO);
            record.setCreateBy(task.getCreateBy());
            record.setCreateTime(task.getCreateTime());
            record.setUpdateBy(task.getUpdateBy());
            record.setUpdateTime(task.getUpdateTime());
            return record;
        }).collect(Collectors.toList()));
    }

    /**
     * @return deviceId -> available List<ActionDTO>
     */
    private Map<String, List<ActionDTO>> assignDeviceTasks(Plan plan) {
        HashMap<String, List<ActionDTO>> result = new HashMap<>();
        List<String> enabledDeviceIds = plan.getDevices().stream()
                .filter(PlanDevice::isEnabled).map(PlanDevice::getDeviceId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(enabledDeviceIds)) {
            return result;
        }
        List<String> enabledActionIds = plan.getActions().stream()
                .filter(PlanAction::isEnabled).map(PlanAction::getActionId)
                .collect(Collectors.toList());
        List<ActionDTO> availableActions = actionService.listAvailableActionDTOByIds(enabledActionIds);
        if (CollectionUtils.isEmpty(availableActions)) {
            return result;
        }

        if (RunMode.COMPATIBLE.equals(plan.getRunMode())) {
            // 兼容模式，执行同一份
            for (String deviceId : enabledDeviceIds) {
                result.put(deviceId, availableActions);
            }
        } else if (RunMode.EFFICIENT.equals(plan.getRunMode())) {
            // 高效模式，平均分
            int i = 0;
            int deviceCount = enabledDeviceIds.size();
            for (ActionDTO action : availableActions) {
                if (i == deviceCount) i = 0;
                String deviceId = enabledDeviceIds.get(i);
                List<ActionDTO> actions = result.computeIfAbsent(deviceId, k -> new ArrayList<>());
                actions.add(action);
                i++;
            }
        }
        return result;
    }
}
