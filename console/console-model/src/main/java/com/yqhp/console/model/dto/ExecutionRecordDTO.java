package com.yqhp.console.model.dto;

import com.yqhp.common.web.model.OutputConverter;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.repository.entity.ExecutionRecord;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class ExecutionRecordDTO extends ExecutionRecord
        implements OutputConverter<ExecutionRecordDTO, ExecutionRecord> {
    private List<DeviceTask> tasks;
}
