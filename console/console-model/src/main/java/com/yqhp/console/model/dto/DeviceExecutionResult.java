package com.yqhp.console.model.dto;

import com.yqhp.console.repository.entity.Device;
import com.yqhp.console.repository.entity.DeviceTask;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class DeviceExecutionResult {
    private Device device;
    private List<DeviceTask> tasks;
}
