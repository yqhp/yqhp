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
package com.yqhp.auth.rpc;

import com.yqhp.auth.model.dto.UserInfo;
import com.yqhp.auth.model.param.UpdateUserParam;
import com.yqhp.auth.model.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jiangyitao
 */
@FeignClient(name = "auth-service", path = "/auth/user", contextId = "user")
public interface UserRpc {
    @PostMapping("/changePassword")
    void changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword);

    @GetMapping("/info")
    UserInfo info();

    @PutMapping
    void updateUser(@RequestBody UpdateUserParam updateUserParam);

    @GetMapping("/{userId}")
    UserVO getVOById(@PathVariable("userId") String userId);

    @PostMapping("/users")
    List<UserVO> listVOByIds(@RequestBody Set<String> userIds);

    @PostMapping("/usersMap")
    Map<String, UserVO> getVOMapByIds(@RequestBody Set<String> userIds);
}
