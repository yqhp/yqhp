package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.dto.DeviceDocExecutionResult;
import com.yqhp.console.repository.entity.DocExecutionRecord;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface DocExecutionRecordService extends IService<DocExecutionRecord> {
    List<DocExecutionRecord> listByExecutionRecordId(String executionRecordId);

    List<DocExecutionRecord> listByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId);

    void deleteByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId);

    DeviceDocExecutionResult statDeviceDocExecutionResult(List<DocExecutionRecord> records);
}
