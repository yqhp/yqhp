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
import com.yqhp.auth.model.dto.RoleDTO;
import com.yqhp.auth.model.param.RoleParam;
import com.yqhp.auth.repository.entity.Role;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface RoleService extends IService<Role> {
    List<Role> listByUserId(String userId);

    List<RoleDTO> listDTOByUserId(String userId);

    Role getRoleById(String roleId);

    void createRole(RoleParam param);

    void updateRole(String id, RoleParam param);

    void deleteById(String id);
}
