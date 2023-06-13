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
package com.yqhp.auth.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.auth.model.dto.RoleDTO;
import com.yqhp.auth.model.param.RoleParam;
import com.yqhp.auth.repository.entity.Role;
import com.yqhp.auth.repository.entity.RoleAuthority;
import com.yqhp.auth.repository.mapper.RoleMapper;
import com.yqhp.auth.web.enums.ResponseCodeEnum;
import com.yqhp.auth.web.service.RoleAuthorityService;
import com.yqhp.auth.web.service.RoleService;
import com.yqhp.auth.web.service.UserRoleService;
import com.yqhp.common.web.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private Snowflake snowflake;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RoleAuthorityService roleAuthorityService;

    @Override
    public List<Role> listByUserId(String userId) {
        List<String> roleIds = userRoleService.listRoleIdByUserId(userId);
        return roleIds.isEmpty() ? new ArrayList<>() : listByIds(roleIds); // mybatis-plus很变态，不能传empty的集合进去
    }

    @Override
    public List<RoleDTO> listDTOByUserId(String userId) {
        List<Role> roles = listByUserId(userId);
        return toRoleDTOs(roles);
    }

    @Override
    public Role getRoleById(String roleId) {
        return Optional.ofNullable(getById(roleId))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.ROLE_NOT_FOUND));
    }

    @Override
    public void createRole(RoleParam param) {
        Role role = param.convertTo();
        role.setId(snowflake.nextIdStr());
        String currUid = CurrentUser.id();
        role.setCreateBy(currUid);
        role.setUpdateBy(currUid);

        try {
            if (!save(role)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_ROLE_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ROLE);
        }
    }

    @Override
    public void updateRole(String id, RoleParam param) {
        Role role = getRoleById(id);
        param.update(role);
        role.setUpdateBy(CurrentUser.id());
        role.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(role)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_ROLE_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ROLE);
        }
    }

    @Override
    public void deleteById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_ROLE_FAIL);
        }
    }

    private List<RoleDTO> toRoleDTOs(List<Role> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return new ArrayList<>();
        }

        List<String> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        // roleId -> List<RoleAuthority>
        Map<String, List<RoleAuthority>> roleAuthorityMap = roleAuthorityService.listInRoleIds(roleIds)
                .stream().collect(Collectors.groupingBy(RoleAuthority::getRoleId));

        return roles.stream().map((role) -> {
            RoleDTO roleDTO = new RoleDTO().convertFrom(role);
            roleDTO.setAuthorities(roleAuthorityMap.get(role.getId()));
            return roleDTO;
        }).collect(Collectors.toList());
    }
}
