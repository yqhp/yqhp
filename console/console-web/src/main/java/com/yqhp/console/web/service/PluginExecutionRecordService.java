package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.dto.DevicePluginExecutionResult;
import com.yqhp.console.repository.entity.PluginExecutionRecord;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface PluginExecutionRecordService extends IService<PluginExecutionRecord> {
    List<PluginExecutionRecord> listByExecutionRecordId(String executionRecordId);

    List<PluginExecutionRecord> listByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId);

    void deleteByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId);

    DevicePluginExecutionResult statDevicePluginExecutionResult(List<PluginExecutionRecord> records);
}
