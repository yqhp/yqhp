package com.yqhp.common.kafka.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.StepExecutionRecord;
import com.yqhp.console.repository.enums.StepExecutionStatus;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StepExecutionRecordMessage implements InputConverter<StepExecutionRecord> {
    private String id;
    private String deviceId;
    private StepExecutionStatus status;
    private Long startTime;
    private Long endTime;
    private List<JShellEvalResult> results;
}
