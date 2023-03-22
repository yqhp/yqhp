package com.yqhp.console.model.dto;

import com.yqhp.common.web.model.OutputConverter;
import com.yqhp.console.repository.entity.PlanExecutionRecord;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class PlanExecutionRecordDTO extends PlanExecutionRecord
        implements OutputConverter<PlanExecutionRecordDTO, PlanExecutionRecord> {
    private List<DeviceTaskDTO> tasks;
}
