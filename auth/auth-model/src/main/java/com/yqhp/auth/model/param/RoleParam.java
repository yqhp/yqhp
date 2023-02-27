package com.yqhp.auth.model.param;

import com.yqhp.auth.repository.entity.Role;
import com.yqhp.common.web.model.InputConverter;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class RoleParam implements InputConverter<Role> {
    @NotBlank(message = "角色名不能为空")
    @Size(max = 128, message = "角色名长度不能超过{max}")
    private String name;
}
