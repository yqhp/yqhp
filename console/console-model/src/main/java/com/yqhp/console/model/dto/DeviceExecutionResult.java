package com.yqhp.console.model.dto;

import com.yqhp.console.repository.enums.ExecutionStatus;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class DeviceExecutionResult {
    private String deviceId;
    private Long startTime = 0L;
    private Long endTime = 0L;
    private boolean isFinished;
    private ExecutionStatus status;
    private DevicePluginExecutionResult pluginExecutionResult;
    private DeviceDocExecutionResult docExecutionResult;
}
