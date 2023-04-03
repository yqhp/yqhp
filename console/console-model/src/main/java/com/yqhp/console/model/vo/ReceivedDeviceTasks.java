package com.yqhp.console.model.vo;

import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class ReceivedDeviceTasks {
    private List<DeviceTask> tasks;
    private PlanExecutionRecord planExecutionRecord;
}
