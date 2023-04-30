package com.yqhp.console.model.dto;

import com.yqhp.console.model.enums.DevicePluginExecutionStatus;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class DevicePluginExecutionResult {
    private boolean isFinished;
    private DevicePluginExecutionStatus status;
}
