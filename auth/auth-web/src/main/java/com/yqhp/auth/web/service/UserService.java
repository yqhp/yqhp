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
    User createUser(CreateUserParam param);

    void deleteById(String userId);

    User updateUser(String userId, UpdateUserParam param);

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
