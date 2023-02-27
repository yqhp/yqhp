package com.yqhp.auth.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.auth.model.dto.RoleDTO;
import com.yqhp.auth.model.param.RoleParam;
import com.yqhp.auth.repository.entity.Role;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface RoleService extends IService<Role> {
    List<Role> listByUserId(String userId);

    List<RoleDTO> listRoleDTOByUserId(String userId);

    Role getRoleById(String roleId);

    void createRole(RoleParam param);

    void updateRole(String id, RoleParam param);

    void deleteRoleById(String id);
}
