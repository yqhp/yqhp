package com.yqhp.auth.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.auth.model.param.UserRoleParam;
import com.yqhp.auth.repository.entity.UserRole;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface UserRoleService extends IService<UserRole> {
    UserRole createUserRole(UserRoleParam userRoleParam);

    void deleteUserRole(UserRoleParam userRoleParam);

    List<UserRole> listByUserId(String userId);

    List<String> listRoleIdByUserId(String userId);
}
