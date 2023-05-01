package com.yqhp.console.model.dto;

import com.yqhp.console.repository.enums.ExecutionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class ExecutionResult {
    private String id;
    private LocalDateTime createTime;
    private Long startTime = 0L;
    private Long endTime = 0L;
    private boolean isFinished;
    private ExecutionStatus status;
    private List<DeviceExecutionResult> deviceExecutionResults;
}
