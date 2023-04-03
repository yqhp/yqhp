package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import com.yqhp.console.repository.mapper.DeviceTaskMapper;
import com.yqhp.console.web.service.DeviceTaskService;
import com.yqhp.console.web.service.PlanExecutionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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

    @Override
    public void cachePlanExecutionRecordForDevice(String deviceId, String planExecutionRecordId) {
        redisTemplate.opsForList().leftPush(getPlanExecutionRecordKey(deviceId), planExecutionRecordId);
    }

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

        ReceivedDeviceTasks received = new ReceivedDeviceTasks();
        received.setPlanExecutionRecord(planExecutionRecord);
        received.setTasks(tasks);
        return received;
    }

    @Override
    public List<DeviceTask> listByPlanExecutionRecordId(String planExecutionRecordId) {
        Assert.hasText(planExecutionRecordId, "planExecutionRecordId must has text");
        LambdaQueryWrapper<DeviceTask> query = new LambdaQueryWrapper<>();
        query.eq(DeviceTask::getPlanExecutionRecordId, planExecutionRecordId);
        return list(query);
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
