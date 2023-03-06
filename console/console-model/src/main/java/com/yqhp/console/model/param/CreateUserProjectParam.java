package com.yqhp.console.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateUserProjectParam extends UpdateUserProjectParam {
    @NotBlank(message = "用户不能为空")
    private String userId;
}
