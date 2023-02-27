package com.yqhp.auth.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class CreateRoleAuthorityParam extends UpdateRoleAuthorityParam {
    @NotBlank(message = "角色id不能为空")
    private String roleId;
}
