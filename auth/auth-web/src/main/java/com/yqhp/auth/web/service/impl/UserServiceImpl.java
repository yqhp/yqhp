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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.auth.model.dto.RoleDTO;
import com.yqhp.auth.model.dto.UserInfo;
import com.yqhp.auth.model.param.CreateUserParam;
import com.yqhp.auth.model.param.UpdateUserParam;
import com.yqhp.auth.model.param.query.UserPageQuery;
import com.yqhp.auth.model.vo.UserVO;
import com.yqhp.auth.repository.entity.User;
import com.yqhp.auth.repository.enums.UserStatus;
import com.yqhp.auth.repository.mapper.UserMapper;
import com.yqhp.auth.web.enums.ResponseCodeEnum;
import com.yqhp.auth.web.service.RoleService;
import com.yqhp.auth.web.service.UserService;
import com.yqhp.common.web.exception.ServiceException;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Snowflake snowflake;

    @Override
    public User createUser(CreateUserParam createUserParam) {
        User user = createUserParam.convertTo();
        user.setId(snowflake.nextIdStr());
        user.setPassword(encodePassword(user.getPassword()));

        String currUid = CurrentUser.id();
        user.setCreateBy(currUid);
        user.setUpdateBy(currUid);

        try {
            if (!save(user)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_USER_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_USER);
        }

        return getById(user.getId());
    }

    @Override
    public void deleteById(String userId) {
        User user = getUserById(userId);
        if ("admin".equals(user.getUsername())) {
            throw new ServiceException(ResponseCodeEnum.ADMIN_CANNOT_BE_DELETED);
        }
        if (!removeById(userId)) {
            throw new ServiceException(ResponseCodeEnum.DEL_USER_FAILED);
        }
    }

    @Override
    public User updateUser(String userId, UpdateUserParam updateUserParam) {
        User user = getUserById(userId);
        updateUserParam.update(user);
        user.setUpdateBy(CurrentUser.id());
        user.setUpdateTime(LocalDateTime.now());
        if (!updateById(user)) {
            throw new ServiceException(ResponseCodeEnum.UPDATE_USER_FAILED);
        }
        return getById(userId);
    }

    @Override
    public IPage<UserVO> pageBy(UserPageQuery query) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.eq(query.getStatus() != null, User::getStatus, query.getStatus());
        String keyword = query.getKeyword();
        q.and(StringUtils.hasText(keyword), c -> c
                .like(User::getId, keyword)
                .or()
                .like(User::getUsername, keyword)
                .or()
                .like(User::getNickname, keyword)
        );
        q.orderByDesc(User::getId);
        return page(new Page<>(query.getPageNumb(), query.getPageSize()), q)
                .convert(this::toUserVO);
    }

    @Override
    public void resetPassword(String userId, String password) {
        User user = getUserById(userId);
        resetPassword(user, password);
    }

    private void resetPassword(User user, String password) {
        user.setPassword(encodePassword(password));
        user.setUpdateBy(CurrentUser.id());
        user.setUpdateTime(LocalDateTime.now());
        if (!updateById(user)) {
            throw new ServiceException(ResponseCodeEnum.RESET_PASSWORD_FAILED);
        }
    }

    @Override
    public User getUserById(String userId) {
        return Optional.ofNullable(getById(userId))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.USER_NOT_FOUND));
    }

    @Override
    public UserVO getVOById(String userId) {
        return toUserVO(getUserById(userId));
    }

    @Override
    public User getByUsername(String username) {
        Assert.hasText(username, "username must has text");
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, username);
        return getOne(query);
    }

    @Override
    public UserInfo getInfoByUsername(String username) {
        User user = getByUsername(username);
        return toUserInfo(user);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // 检查旧密码
        User user = getUserById(CurrentUser.id());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ServiceException(ResponseCodeEnum.OLD_PASSWORD_ERROR);
        }
        if (oldPassword.equals(newPassword)) {
            return;
        }
        // 设置新密码
        resetPassword(user, newPassword);
    }

    @Override
    public void changeStatus(String userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        user.setUpdateBy(CurrentUser.id());
        user.setUpdateTime(LocalDateTime.now());
        if (!updateById(user)) {
            throw new ServiceException(ResponseCodeEnum.CHANGE_STATUS_FAILED);
        }
    }

    @Override
    public UserInfo getInfo() {
        UserInfo userInfo = CurrentUser.get();
        userInfo.setPassword(null);
        return userInfo;
    }

    @Override
    public List<UserVO> listVOByIds(Set<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.in(User::getId, userIds);
        return list(query).stream().map(this::toUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, UserVO> getVOMapByIds(Set<String> userIds) {
        return listVOByIds(userIds).stream()
                .collect(Collectors.toMap(UserVO::getId, Function.identity(), (k1, k2) -> k1));
    }

    private UserVO toUserVO(User user) {
        if (user == null) return null;
        return new UserVO().convertFrom(user);
    }

    private UserInfo toUserInfo(User user) {
        if (user == null) return null;
        UserInfo userInfo = new UserInfo().convertFrom(user);
        List<RoleDTO> roles = roleService.listDTOByUserId(userInfo.getId());
        userInfo.setRoles(roles);
        return userInfo;
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

}
