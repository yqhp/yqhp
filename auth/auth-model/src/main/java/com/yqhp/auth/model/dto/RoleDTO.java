package com.yqhp.auth.model.dto;

import com.yqhp.auth.repository.entity.Role;
import com.yqhp.auth.repository.entity.RoleAuthority;
import com.yqhp.common.web.model.OutputConverter;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class RoleDTO extends Role implements OutputConverter<RoleDTO, Role> {
    private List<RoleAuthority> authorities;
}
