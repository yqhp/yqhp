package com.yqhp.console.repository.jsonfield;

import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.repository.entity.Doc;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class ActionStepX extends ActionStep {
    private String executionId;
    private ActionX action;
    private Doc doc;
}
