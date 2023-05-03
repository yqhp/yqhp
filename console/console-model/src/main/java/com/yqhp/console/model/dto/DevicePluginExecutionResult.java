package com.yqhp.console.model.dto;

import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionStatus;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class DevicePluginExecutionResult {
    private Long startTime = 0L;
    private Long endTime = 0L;
    private boolean isFinished;
    private ExecutionStatus status;
    private List<PluginExecutionRecord> records;
}