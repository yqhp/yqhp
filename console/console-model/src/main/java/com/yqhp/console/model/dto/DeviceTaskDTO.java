package com.yqhp.console.model.dto;

import com.yqhp.common.web.model.OutputConverter;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.StepExecutionRecord;
import lombok.Data;

import java.util.List;

@Data
public class DeviceTaskDTO extends DeviceTask implements OutputConverter<DeviceTaskDTO, DeviceTask> {
    private List<StepExecutionRecord> records;
}
