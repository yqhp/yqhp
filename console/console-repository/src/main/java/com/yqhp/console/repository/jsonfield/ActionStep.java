package com.yqhp.console.repository.jsonfield;

import com.yqhp.console.repository.enums.ActionStepErrorHandler;
import com.yqhp.console.repository.enums.ActionStepType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author jiangyitao
 */
@Data
public class ActionStep {
    @NotNull(message = "步骤类型不能为空")
    private ActionStepType type;
    @NotBlank(message = "步骤id不能为空")
    private String idOfType;
    private String name;
    @NotNull(message = "错误处理方式不能为空")
    private ActionStepErrorHandler errorHandler;
    private boolean enabled;
}
