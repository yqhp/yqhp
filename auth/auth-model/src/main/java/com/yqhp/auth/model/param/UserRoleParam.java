package com.yqhp.auth.model.param;

import com.yqhp.auth.repository.entity.UserRole;
import com.yqhp.common.web.model.InputConverter;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class UserRoleParam implements InputConverter<UserRole> {
    @NotBlank(message = "用户不能为空")
    private String userId;
    @NotBlank(message = "角色不能为空")
    private String roleId;
}
