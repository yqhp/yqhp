package com.yqhp.console.web.service.impl;

import com.yqhp.console.model.vo.DeviceTask;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.web.service.DeviceTaskService;
import com.yqhp.console.web.service.DocExecutionRecordService;
import com.yqhp.console.web.service.ExecutionRecordService;
import com.yqhp.console.web.service.PluginExecutionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class DeviceTaskServiceImpl implements DeviceTaskService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ExecutionRecordService executionRecordService;
    @Autowired
    private PluginExecutionRecordService pluginExecutionRecordService;
    @Autowired
    private DocExecutionRecordService docExecutionRecordService;

    @Override
    public void push(Set<String> deviceIds, String executionRecordId) {
        for (String deviceId : deviceIds) {
            redisTemplate.opsForList().leftPush(getRedisKey(deviceId), executionRecordId);
        }
    }

    @Override
    public DeviceTask receive(String deviceId) {
        String executionRecordId = redisTemplate.opsForList().rightPop(getRedisKey(deviceId));
        if (executionRecordId == null) {
            return null;
        }

        log.info("device={}, received executionRecordId={}", deviceId, executionRecordId);
        ExecutionRecord executionRecord = executionRecordService.getById(executionRecordId);
        if (executionRecord == null) {
            return null;
        }

        DeviceTask task = new DeviceTask();
        task.setExecutionRecord(executionRecord);
        List<PluginExecutionRecord> pluginExecutionRecords = pluginExecutionRecordService
                .listByExecutionRecordIdAndDeviceId(executionRecordId, deviceId);
        task.setPluginExecutionRecords(pluginExecutionRecords);
        List<DocExecutionRecord> docExecutionRecords = docExecutionRecordService
                .listByExecutionRecordIdAndDeviceId(executionRecordId, deviceId);
        task.setDocExecutionRecords(docExecutionRecords);
        return task;
    }

    private String getRedisKey(String deviceId) {
        return "executionRecord:" + deviceId;
    }
}
