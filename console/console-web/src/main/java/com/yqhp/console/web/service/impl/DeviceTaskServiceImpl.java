package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.model.dto.DeviceExecutionResult;
import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import com.yqhp.console.repository.entity.Device;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import com.yqhp.console.repository.enums.DocKind;
import com.yqhp.console.repository.mapper.DeviceTaskMapper;
import com.yqhp.console.web.service.DeviceService;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
    @Autowired
    private DeviceService deviceService;

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
    public List<DeviceExecutionResult> listDeviceExecutionResultByExecutionRecordId(String executionRecordId) {
        List<DeviceTask> tasks = listByExecutionRecordId(executionRecordId);
        return toDeviceExecutionResult(tasks);
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
    public boolean isDeviceFinished(List<DeviceTask> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return true;
        }
        boolean initFailed = tasks.stream()
                .filter(task -> DocKind.JSH_INIT.equals(task.getDocKind()))
                .anyMatch(task -> DeviceTaskStatus.FAILED.equals(task.getStatus()));
        if (initFailed) {
            return true;
        }
        return tasks.stream()
                .allMatch(task -> DeviceTaskStatus.SUCCESSFUL.equals(task.getStatus())
                        || DeviceTaskStatus.FAILED.equals(task.getStatus()));
    }

    private String getExecutionRecordKey(String deviceId) {
        return "executionRecord:" + deviceId;
    }

    private List<DeviceExecutionResult> toDeviceExecutionResult(List<DeviceTask> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return new ArrayList<>();
        }

        Set<String> deviceIds = tasks.stream()
                .map(DeviceTask::getDeviceId).collect(Collectors.toSet());
        // deviceId -> Device
        Map<String, Device> deviceMap = deviceService.listByIds(deviceIds).stream()
                .collect(Collectors.toMap(Device::getId, Function.identity(), (k1, k2) -> k1));

        // deviceId -> List<DeviceTask>
        Map<String, List<DeviceTask>> tasksMap = tasks.stream()
                .collect(Collectors.groupingBy(DeviceTask::getDeviceId));
        List<DeviceExecutionResult> results = new ArrayList<>(deviceIds.size());
        tasksMap.forEach((deviceId, deviceTasks) -> {
            DeviceExecutionResult result = new DeviceExecutionResult();
            result.setDevice(deviceMap.get(deviceId));
            result.setTasks(deviceTasks);
            results.add(result);
        });

        return results;
    }
}
