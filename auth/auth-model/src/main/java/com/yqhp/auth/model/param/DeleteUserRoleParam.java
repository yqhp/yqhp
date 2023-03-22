package com.yqhp.auth.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class DeleteUserRoleParam {
    @NotBlank(message = "用户不能为空")
    private String userId;
    @NotBlank(message = "角色不能为空")
    private String roleId;
}
