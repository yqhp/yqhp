package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.repository.entity.StepExecutionRecord;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import com.yqhp.console.repository.enums.StepExecutionStatus;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;
import com.yqhp.console.repository.mapper.DeviceTaskMapper;
import com.yqhp.console.web.service.DeviceTaskService;
import com.yqhp.console.web.service.PlanExecutionRecordService;
import com.yqhp.console.web.service.StepExecutionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class DeviceTaskServiceImpl extends ServiceImpl<DeviceTaskMapper, DeviceTask>
        implements DeviceTaskService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PlanExecutionRecordService planExecutionRecordService;
    @Autowired
    private StepExecutionRecordService stepExecutionRecordService;

    @Override
    public void cachePlanExecutionRecordIdForDevice(String deviceId, String planExecutionRecordId) {
        redisTemplate.opsForList().leftPush(getPlanExecutionRecordKey(deviceId), planExecutionRecordId);
    }

    /**
     * 领取测试任务
     */
    @Transactional
    @Override
    public ReceivedDeviceTasks receive(String deviceId) {
        String planExecutionRecordId = redisTemplate.opsForList().rightPop(getPlanExecutionRecordKey(deviceId));
        if (planExecutionRecordId == null) {
            return null;
        }
        log.info("device={}, received planExecutionRecordId={}", deviceId, planExecutionRecordId);
        PlanExecutionRecord planExecutionRecord = planExecutionRecordService.getById(planExecutionRecordId);
        if (planExecutionRecord == null) {
            return null;
        }
        LambdaQueryWrapper<DeviceTask> tasksQuery = new LambdaQueryWrapper<>();
        tasksQuery.eq(DeviceTask::getPlanExecutionRecordId, planExecutionRecordId)
                .eq(DeviceTask::getDeviceId, deviceId);
        List<DeviceTask> tasks = list(tasksQuery);
        if (tasks.isEmpty()) {
            return null;
        }

        // 更新设备任务状态
        List<DeviceTask> toUpdateTasks = tasks.stream().map(task -> {
            DeviceTask toUpdateTask = new DeviceTask();
            toUpdateTask.setId(task.getId());
            toUpdateTask.setStatus(DeviceTaskStatus.RECEIVED);
            return toUpdateTask;
        }).collect(Collectors.toList());
        updateBatchById(toUpdateTasks);

        List<String> deviceTaskIds = tasks.stream().map(DeviceTask::getId).collect(Collectors.toList());

        // 更新步骤执行记录状态
        List<StepExecutionRecord> stepExecutionRecords = stepExecutionRecordService.listInDeviceTaskIds(deviceTaskIds);
        List<StepExecutionRecord> toUpdateRecords = stepExecutionRecords.stream().map(record -> {
            StepExecutionRecord toUpdateRecord = new StepExecutionRecord();
            toUpdateRecord.setId(record.getId());
            toUpdateRecord.setStatus(StepExecutionStatus.RECEIVED);
            return toUpdateRecord;
        }).collect(Collectors.toList());
        stepExecutionRecordService.updateBatchById(toUpdateRecords);

        // taskId -> records
        Map<String, List<StepExecutionRecord>> recordsMap = stepExecutionRecords.stream()
                .collect(Collectors.groupingBy(StepExecutionRecord::getDeviceTaskId));

        ReceivedDeviceTasks received = new ReceivedDeviceTasks();
        received.setPlanExecutionRecord(planExecutionRecord);
        received.setTasks(tasks.stream().map(task -> {
            ReceivedDeviceTasks.Task rTask = new ReceivedDeviceTasks.Task();
            rTask.setId(task.getId());
            ActionDTO action = new ActionDTO();
            BeanUtils.copyProperties(task.getAction(), action);
            action.setSteps(recordsMap.get(task.getId()).stream()
                    .map(record -> {
                        ActionStepDTO step = record.getStep();
                        step.setExecutionId(record.getId());
                        return step;
                    }).collect(Collectors.toList())
            );
            rTask.setAction(action);
            return rTask;
        }).collect(Collectors.toList()));
        return received;
    }

    @Override
    public List<DeviceTask> listInPlanExecutionRecordIds(List<String> planExecutionRecordIds) {
        if (CollectionUtils.isEmpty(planExecutionRecordIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DeviceTask> query = new LambdaQueryWrapper<>();
        query.in(DeviceTask::getPlanExecutionRecordId, planExecutionRecordIds);
        return list(query);
    }

    @Override
    public boolean isFinished(DeviceTask task) {
        return DeviceTaskStatus.SUCCESSFUL.equals(task.getStatus())
                || DeviceTaskStatus.FAILED.equals(task.getStatus());
    }

    private String getPlanExecutionRecordKey(String deviceId) {
        return "planExecutionRecord:" + deviceId;
    }
}
