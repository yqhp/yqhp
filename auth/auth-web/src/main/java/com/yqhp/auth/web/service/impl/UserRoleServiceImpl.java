package com.yqhp.auth.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.auth.model.param.UserRoleParam;
import com.yqhp.auth.repository.entity.UserRole;
import com.yqhp.auth.repository.mapper.UserRoleMapper;
import com.yqhp.auth.web.enums.ResponseCodeEnum;
import com.yqhp.auth.web.service.UserRoleService;
import com.yqhp.common.web.exception.ServiceException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Override
    public UserRole createUserRole(UserRoleParam userRoleParam) {
        UserRole userRole = userRoleParam.convertTo();

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

        return userRole;
    }

    @Override
    public void deleteUserRole(UserRoleParam userRoleParam) {
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.eq(UserRole::getUserId, userRoleParam.getUserId())
                .eq(UserRole::getRoleId, userRoleParam.getRoleId());
        if (!remove(query)) {
            throw new ServiceException(ResponseCodeEnum.DEL_USER_ROLE_FAIL);
        }
    }

    @Override
    public List<UserRole> listByUserId(String userId) {
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
