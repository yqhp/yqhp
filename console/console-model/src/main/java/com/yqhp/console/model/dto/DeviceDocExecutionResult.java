package com.yqhp.console.model.dto;

import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionStatus;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class DeviceDocExecutionResult {
    private Long startTime = 0L;
    private Long endTime = 0L;
    private boolean isFinished;
    private ExecutionStatus status;
    private List<DocExecutionRecord> records;
}
