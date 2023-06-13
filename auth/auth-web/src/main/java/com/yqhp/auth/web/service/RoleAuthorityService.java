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

    void deleteById(String id);

    List<RoleAuthority> listByRoleId(String roleId);
}
