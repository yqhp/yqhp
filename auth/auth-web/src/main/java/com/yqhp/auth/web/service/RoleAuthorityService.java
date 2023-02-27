package com.yqhp.auth.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.auth.model.param.CreateRoleAuthorityParam;
import com.yqhp.auth.model.param.UpdateRoleAuthorityParam;
import com.yqhp.auth.repository.entity.RoleAuthority;

import java.util.Collection;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface RoleAuthorityService extends IService<RoleAuthority> {
    List<RoleAuthority> listInRoleIds(Collection<String> roleIds);

    RoleAuthority getRoleAuthorityById(String id);

    void createRoleAuthority(CreateRoleAuthorityParam param);

    void updateRoleAuthority(String id, UpdateRoleAuthorityParam param);

    void deleteRoleAuthorityById(String id);

    List<RoleAuthority> listByRoleId(String roleId);
}
