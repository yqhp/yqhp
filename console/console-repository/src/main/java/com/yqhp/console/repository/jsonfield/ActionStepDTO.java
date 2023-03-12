package com.yqhp.console.repository.jsonfield;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.repository.entity.Doc;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionStepDTO extends ActionStep {
    private String executionId;
    private ActionDTO action;
    private Doc doc;
}
