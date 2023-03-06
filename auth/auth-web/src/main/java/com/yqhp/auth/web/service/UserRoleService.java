package com.yqhp.auth.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.auth.model.param.CreateUserRoleParam;
import com.yqhp.auth.model.param.UpdateUserRoleParam;
import com.yqhp.auth.repository.entity.UserRole;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface UserRoleService extends IService<UserRole> {
    void createUserRole(CreateUserRoleParam param);

    void updateUserRole(String id, UpdateUserRoleParam param);

    void deleteUserRoleById(String id);

    UserRole getUserRoleById(String id);

    List<UserRole> listByUserId(String userId);

    List<String> listRoleIdByUserId(String userId);
}
