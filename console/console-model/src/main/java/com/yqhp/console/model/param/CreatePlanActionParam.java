package com.yqhp.console.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreatePlanActionParam extends UpdatePlanActionParam {
    @NotBlank(message = "planId不能为空")
    private String planId;
}
