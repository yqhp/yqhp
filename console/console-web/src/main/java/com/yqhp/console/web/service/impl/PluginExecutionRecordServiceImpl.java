package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.PluginExecutionRecordStatus;
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
    public boolean isFinished(List<PluginExecutionRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            return true;
        }
        boolean anyFailed = records.stream()
                .anyMatch(record -> PluginExecutionRecordStatus.FAILED.equals(record.getStatus()));
        if (anyFailed) {
            return true;
        }
        return records.stream()
                .allMatch(record -> PluginExecutionRecordStatus.SUCCESSFUL.equals(record.getStatus()));
    }
}
