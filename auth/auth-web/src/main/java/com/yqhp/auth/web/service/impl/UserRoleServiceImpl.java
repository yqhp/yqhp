package com.yqhp.auth.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.auth.model.param.CreateUserRoleParam;
import com.yqhp.auth.model.param.UpdateUserRoleParam;
import com.yqhp.auth.repository.entity.UserRole;
import com.yqhp.auth.repository.mapper.UserRoleMapper;
import com.yqhp.auth.web.enums.ResponseCodeEnum;
import com.yqhp.auth.web.service.UserRoleService;
import com.yqhp.common.web.exception.ServiceException;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Autowired
    private Snowflake snowflake;

    @Override
    public void createUserRole(CreateUserRoleParam param) {
        UserRole userRole = param.convertTo();
        userRole.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        userRole.setCreateBy(currUid);
        userRole.setUpdateBy(currUid);

        try {
            if (!save(userRole)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_USER_ROLE_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_USER_ROLE);
        }
    }

    @Override
    public void updateUserRole(String id, UpdateUserRoleParam param) {
        UserRole userRole = getUserRoleById(id);
        param.update(userRole);
        userRole.setUpdateBy(CurrentUser.id());
        userRole.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(userRole)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_USER_ROLE_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_USER_ROLE);
        }
    }

    @Override
    public void deleteUserRoleById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_USER_ROLE_FAIL);
        }
    }

    @Override
    public UserRole getUserRoleById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.USER_ROLE_NOT_FOUND));
    }

    @Override
    public List<UserRole> listByUserId(String userId) {
        Assert.hasText(userId, "userId must has text");
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.eq(UserRole::getUserId, userId);
        return list(query);
    }

    @Override
    public List<String> listRoleIdByUserId(String userId) {
        return listByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
    }
}
