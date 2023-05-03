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
    // for action
    private Long passCount = 0L;
    private Long failureCount = 0L;
    private Integer totalCount = 0;
    private String passRate;

    private Long startTime = 0L;
    private Long endTime = 0L;
    private boolean isFinished;
    private ExecutionStatus status;
    private List<DocExecutionRecord> records;
}
