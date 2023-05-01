package com.yqhp.common.kafka.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.enums.ExecutionStatus;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocExecutionRecordMessage implements InputConverter<DocExecutionRecord> {
    private String id;
    private String deviceId;
    private ExecutionStatus status;
    private Long startTime;
    private Long endTime;
    private List<JShellEvalResult> results;
}
