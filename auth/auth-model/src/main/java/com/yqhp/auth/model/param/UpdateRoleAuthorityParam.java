package com.yqhp.auth.model.param;

import com.yqhp.auth.repository.entity.RoleAuthority;
import com.yqhp.common.web.model.InputConverter;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class UpdateRoleAuthorityParam implements InputConverter<RoleAuthority> {
    @NotBlank(message = "权限名不能为空")
    @Size(max = 32, message = "权限名长度不能超过{max}")
    private String authorityName;

    @NotBlank(message = "权限值不能为空")
    @Size(max = 32, message = "权限值长度不能超过{max}")
    private String authorityValue;
}
