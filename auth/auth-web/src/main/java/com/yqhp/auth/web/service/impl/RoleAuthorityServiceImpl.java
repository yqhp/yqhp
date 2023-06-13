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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.auth.model.param.CreateRoleAuthorityParam;
import com.yqhp.auth.model.param.UpdateRoleAuthorityParam;
import com.yqhp.auth.repository.entity.RoleAuthority;
import com.yqhp.auth.repository.mapper.RoleAuthorityMapper;
import com.yqhp.auth.web.enums.ResponseCodeEnum;
import com.yqhp.auth.web.service.RoleAuthorityService;
import com.yqhp.common.web.exception.ServiceException;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author jiangyitao
 */
@Service
public class RoleAuthorityServiceImpl
        extends ServiceImpl<RoleAuthorityMapper, RoleAuthority>
        implements RoleAuthorityService {

    @Autowired
    private Snowflake snowflake;

    @Override
    public List<RoleAuthority> listInRoleIds(Collection<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<RoleAuthority> query = new LambdaQueryWrapper<>();
        query.in(RoleAuthority::getRoleId, roleIds);
        return list(query);
    }

    @Override
    public RoleAuthority getRoleAuthorityById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.ROLE_AUTHORITY_NOT_FOUND));
    }

    @Override
    public void createRoleAuthority(CreateRoleAuthorityParam param) {
        RoleAuthority roleAuthority = param.convertTo();
        roleAuthority.setId(snowflake.nextIdStr());
        String currUid = CurrentUser.id();
        roleAuthority.setCreateBy(currUid);
        roleAuthority.setUpdateBy(currUid);

        try {
            if (!save(roleAuthority)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_ROLE_AUTHORITY_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ROLE_AUTHORITY);
        }
    }

    @Override
    public void updateRoleAuthority(String id, UpdateRoleAuthorityParam param) {
        RoleAuthority roleAuthority = getRoleAuthorityById(id);
        param.update(roleAuthority);
        roleAuthority.setUpdateBy(CurrentUser.id());
        roleAuthority.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(roleAuthority)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_ROLE_AUTHORITY_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_ROLE_AUTHORITY);
        }
    }

    @Override
    public void deleteById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_ROLE_AUTHORITY_FAIL);
        }
    }

    @Override
    public List<RoleAuthority> listByRoleId(String roleId) {
        Assert.hasText(roleId, "roleId must has text");
        LambdaQueryWrapper<RoleAuthority> query = new LambdaQueryWrapper<>();
        query.eq(RoleAuthority::getRoleId, roleId);
        return list(query);
    }
}
