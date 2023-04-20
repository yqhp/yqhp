package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.repository.entity.DocExecutionRecord;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface DocExecutionRecordService extends IService<DocExecutionRecord> {
    List<DocExecutionRecord> listByExecutionRecordId(String executionRecordId);

    List<DocExecutionRecord> listByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId);

    boolean isFinished(List<DocExecutionRecord> records);
}
