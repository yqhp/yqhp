package com.yqhp.common.kafka.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionStatus;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginExecutionRecordMessage implements InputConverter<PluginExecutionRecord> {
    private String id;
    private String deviceId;
    private ExecutionStatus status;
    private Long startTime;
    private Long endTime;
}
