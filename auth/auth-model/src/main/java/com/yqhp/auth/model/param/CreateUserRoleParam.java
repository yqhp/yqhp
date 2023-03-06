package com.yqhp.auth.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class CreateUserRoleParam extends UpdateUserRoleParam {
    @NotBlank(message = "用户不能为空")
    private String userId;
}
