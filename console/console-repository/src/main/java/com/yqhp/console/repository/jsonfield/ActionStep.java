package com.yqhp.console.repository.jsonfield;

import com.yqhp.console.repository.enums.ActionStepErrorHandler;
import com.yqhp.console.repository.enums.ActionStepType;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author jiangyitao
 */
@Data
public class ActionStep {
    @NotNull(message = "步骤类型不能为空")
    private ActionStepType type;
    private String idOfType;
    private String name;
    private String content; // jshell
    @NotNull(message = "错误处理方式不能为空")
    private ActionStepErrorHandler errorHandler;
    private boolean enabled;
}
