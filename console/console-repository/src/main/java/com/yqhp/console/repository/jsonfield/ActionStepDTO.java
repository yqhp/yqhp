package com.yqhp.console.repository.jsonfield;

import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.repository.entity.Doc;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class ActionStepDTO extends ActionStep {
    private String executionId;
    private ActionDTO action;
    private Doc doc;
}
