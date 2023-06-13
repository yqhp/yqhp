/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.auth.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.auth.model.param.CreateUserRoleParam;
import com.yqhp.auth.model.param.DeleteUserRoleParam;
import com.yqhp.auth.model.param.UpdateUserRoleParam;
import com.yqhp.auth.repository.entity.UserRole;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface UserRoleService extends IService<UserRole> {
    void createUserRole(CreateUserRoleParam param);

    void updateUserRole(String id, UpdateUserRoleParam param);

    void deleteById(String id);

    void deleteUserRole(DeleteUserRoleParam param);

    UserRole getUserRoleById(String id);

    List<UserRole> listByUserId(String userId);

    List<String> listRoleIdByUserId(String userId);
}