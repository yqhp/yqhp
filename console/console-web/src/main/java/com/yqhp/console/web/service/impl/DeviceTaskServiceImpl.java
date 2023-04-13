package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import com.yqhp.console.repository.mapper.DeviceTaskMapper;
import com.yqhp.console.web.service.DeviceTaskService;
import com.yqhp.console.web.service.ExecutionRecordService;
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
    private ExecutionRecordService executionRecordService;

    @Override
    public void cacheExecutionRecordForDevice(String deviceId, String executionRecordId) {
        redisTemplate.opsForList().leftPush(getExecutionRecordKey(deviceId), executionRecordId);
    }

    @Transactional
    @Override
    public ReceivedDeviceTasks receive(String deviceId) {
        String executionRecordId = redisTemplate.opsForList().rightPop(getExecutionRecordKey(deviceId));
        if (executionRecordId == null) {
            return null;
        }
        log.info("device={}, received executionRecordId={}", deviceId, executionRecordId);
        ExecutionRecord executionRecord = executionRecordService.getById(executionRecordId);
        if (executionRecord == null) {
            return null;
        }
        LambdaQueryWrapper<DeviceTask> tasksQuery = new LambdaQueryWrapper<>();
        tasksQuery.eq(DeviceTask::getExecutionRecordId, executionRecordId)
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
        received.setExecutionRecord(executionRecord);
        received.setTasks(tasks);
        return received;
    }

    @Override
    public List<DeviceTask> listByExecutionRecordId(String executionRecordId) {
        Assert.hasText(executionRecordId, "executionRecordId must has text");
        LambdaQueryWrapper<DeviceTask> query = new LambdaQueryWrapper<>();
        query.eq(DeviceTask::getExecutionRecordId, executionRecordId);
        return list(query);
    }

    @Override
    public List<DeviceTask> listInExecutionRecordIds(List<String> executionRecordIds) {
        if (CollectionUtils.isEmpty(executionRecordIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DeviceTask> query = new LambdaQueryWrapper<>();
        query.in(DeviceTask::getExecutionRecordId, executionRecordIds);
        return list(query);
    }

    @Override
    public boolean isFinished(DeviceTask task) {
        return DeviceTaskStatus.SUCCESSFUL.equals(task.getStatus())
                || DeviceTaskStatus.FAILED.equals(task.getStatus());
    }

    private String getExecutionRecordKey(String deviceId) {
        return "executionRecord:" + deviceId;
    }
}
