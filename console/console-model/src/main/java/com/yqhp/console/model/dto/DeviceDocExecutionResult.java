package com.yqhp.console.model.dto;

import com.yqhp.console.model.enums.DeviceDocExecutionStatus;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class DeviceDocExecutionResult {
    private boolean isFinished;
    private DeviceDocExecutionStatus status;
}
