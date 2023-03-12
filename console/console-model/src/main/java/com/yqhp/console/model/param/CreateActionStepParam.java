package com.yqhp.console.model.param;

import com.yqhp.console.repository.enums.ActionStepKind;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateActionStepParam extends UpdateActionStepParam {
    @NotBlank(message = "projectId不能为空")
    private String projectId;
    @NotBlank(message = "actionId不能为空")
    private String actionId;
    @NotNull(message = "kind不能为空")
    private ActionStepKind kind;
}
