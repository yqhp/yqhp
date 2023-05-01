package com.yqhp.auth.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.auth.model.dto.UserInfo;
import com.yqhp.auth.model.param.CreateUserParam;
import com.yqhp.auth.model.param.UpdateUserParam;
import com.yqhp.auth.model.param.query.UserPageQuery;
import com.yqhp.auth.model.vo.UserVO;
import com.yqhp.auth.repository.entity.User;
import com.yqhp.auth.repository.enums.UserStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jiangyitao
 */
public interface UserService extends IService<User> {
    User createUser(CreateUserParam createUserParam);

    void deleteById(String userId);

    User updateUser(String userId, UpdateUserParam updateUserParam);

    IPage<UserVO> pageBy(UserPageQuery query);

    void resetPassword(String userId, String password);

    User getUserById(String userId);

    UserVO getVOById(String userId);

    User getByUsername(String username);

    UserInfo getInfoByUsername(String username);

    void changePassword(String oldPassword, String newPassword);

    void changeStatus(String userId, UserStatus status);

    UserInfo getInfo();

    List<UserVO> listVOByIds(Set<String> userIds);

    Map<String, UserVO> getVOMapByIds(Set<String> userIds);
}
