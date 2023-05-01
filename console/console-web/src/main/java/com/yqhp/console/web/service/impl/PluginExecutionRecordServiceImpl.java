package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.model.dto.DevicePluginExecutionResult;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionStatus;
import com.yqhp.console.repository.mapper.PluginExecutionRecordMapper;
import com.yqhp.console.web.service.PluginExecutionRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author jiangyitao
 */
@Service
public class PluginExecutionRecordServiceImpl
        extends ServiceImpl<PluginExecutionRecordMapper, PluginExecutionRecord>
        implements PluginExecutionRecordService {

    @Override
    public List<PluginExecutionRecord> listByExecutionRecordId(String executionRecordId) {
        Assert.hasText(executionRecordId, "executionRecordId must has text");
        LambdaQueryWrapper<PluginExecutionRecord> query = new LambdaQueryWrapper<>();
        query.eq(PluginExecutionRecord::getExecutionRecordId, executionRecordId);
        return list(query);
    }

    @Override
    public List<PluginExecutionRecord> listByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId) {
        Assert.hasText(executionRecordId, "executionRecordId must has text");
        Assert.hasText(deviceId, "deviceId must has text");
        LambdaQueryWrapper<PluginExecutionRecord> query = new LambdaQueryWrapper<>();
        query.eq(PluginExecutionRecord::getExecutionRecordId, executionRecordId);
        query.eq(PluginExecutionRecord::getDeviceId, deviceId);
        return list(query);
    }

    @Override
    public DevicePluginExecutionResult statDevicePluginExecutionResult(List<PluginExecutionRecord> records) {
        DevicePluginExecutionResult result = new DevicePluginExecutionResult();
        result.setRecords(records);
        if (CollectionUtils.isEmpty(records)) {
            result.setFinished(true);
            result.setStatus(ExecutionStatus.SUCCESSFUL);
            return result;
        }

        PluginExecutionRecord firstRecord = records.get(0);
        result.setStartTime(firstRecord.getStartTime());

        boolean anyFailed = records.stream()
                .anyMatch(record -> ExecutionStatus.FAILED.equals(record.getStatus()));
        if (anyFailed) {
            result.setFinished(true);
            result.setEndTime(getEndTime(true, records));
            result.setStatus(ExecutionStatus.FAILED);
            return result;
        }

        boolean allSuccessful = records.stream()
                .allMatch(record -> ExecutionStatus.SUCCESSFUL.equals(record.getStatus()));
        if (allSuccessful) {
            result.setFinished(true);
            result.setEndTime(getEndTime(false, records));
            result.setStatus(ExecutionStatus.SUCCESSFUL);
            return result;
        }

        result.setFinished(false);
        if (ExecutionStatus.TODO.equals(firstRecord.getStatus())) {
            result.setStatus(firstRecord.getStatus());
        } else {
            result.setStatus(ExecutionStatus.STARTED);
        }
        return result;
    }

    private long getEndTime(boolean anyFailed, List<PluginExecutionRecord> records) {
        if (anyFailed) {
            return records.stream()
                    .mapToLong(PluginExecutionRecord::getEndTime)
                    .max().orElse(0);
        }
        PluginExecutionRecord lastRecord = records.get(records.size() - 1);
        return lastRecord.getEndTime();
    }
}
