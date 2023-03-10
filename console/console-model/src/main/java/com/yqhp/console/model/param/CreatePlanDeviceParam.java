package com.yqhp.console.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreatePlanDeviceParam extends UpdatePlanDeviceParam {
    @NotBlank(message = "planId不能为空")
    private String planId;
}
