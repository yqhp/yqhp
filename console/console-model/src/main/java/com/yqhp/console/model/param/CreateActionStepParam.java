package com.yqhp.console.model.param;

import com.yqhp.console.repository.enums.ActionStepFlag;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateActionStepParam extends UpdateActionStepParam {
    @NotBlank(message = "projectId不能为空")
    private String projectId;
    @NotBlank(message = "actionId不能为空")
    private String actionId;
    @NotNull(message = "flag不能为空")
    private ActionStepFlag flag;
}
