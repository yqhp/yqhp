package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.repository.entity.StepExecutionRecord;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import com.yqhp.console.repository.enums.StepExecutionStatus;
import com.yqhp.console.repository.mapper.DeviceTaskMapper;
import com.yqhp.console.web.service.DeviceTaskService;
import com.yqhp.console.web.service.PlanExecutionRecordService;
import com.yqhp.console.web.service.StepExecutionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

        ReceivedDeviceTasks received = new ReceivedDeviceTasks();
        received.setPlanExecutionRecord(planExecutionRecord);
        received.setDeviceTasks(tasks);

        LocalDateTime now = LocalDateTime.now();
        List<String> deviceTaskIds = new ArrayList<>(tasks.size());
        // 更新任务状态
        for (DeviceTask task : tasks) {
            task.setStatus(DeviceTaskStatus.RECEIVED);
            task.setUpdateTime(now);
            deviceTaskIds.add(task.getId());
        }
        updateBatchById(tasks);
        // 更新步骤执行记录
        List<StepExecutionRecord> stepExecutionRecords = stepExecutionRecordService.listInDeviceTaskIds(deviceTaskIds);
        List<StepExecutionRecord> toUpdateRecords = stepExecutionRecords.stream().map(record -> {
            StepExecutionRecord toUpdateRecord = new StepExecutionRecord();
            toUpdateRecord.setId(record.getId());
            toUpdateRecord.setStatus(StepExecutionStatus.RECEIVED);
            toUpdateRecord.setUpdateTime(now);
            return toUpdateRecord;
        }).collect(Collectors.toList());
        if (!toUpdateRecords.isEmpty()) {
            stepExecutionRecordService.updateBatchById(toUpdateRecords);
        }

        log.info("device={}, received tasks={}", deviceId, deviceTaskIds);
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
