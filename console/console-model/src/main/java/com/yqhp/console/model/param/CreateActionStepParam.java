package com.yqhp.console.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateActionStepParam extends UpdateActionStepParam {
    @NotBlank(message = "projectId不能为空")
    private String projectId;
    @NotBlank(message = "actionId不能为空")
    private String actionId;
}
