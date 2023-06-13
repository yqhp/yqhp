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
package com.yqhp.auth.web.controller;

import com.yqhp.auth.model.param.CreateUserRoleParam;
import com.yqhp.auth.model.param.DeleteUserRoleParam;
import com.yqhp.auth.model.param.UpdateUserRoleParam;
import com.yqhp.auth.repository.entity.UserRole;
import com.yqhp.auth.web.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/userRole")
@Validated
@PreAuthorize("hasAuthority('admin')")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @PostMapping
    public void createUserRole(@Valid @RequestBody CreateUserRoleParam param) {
        userRoleService.createUserRole(param);
    }

    @PutMapping("/{id}")
    public void updateUserRole(@PathVariable String id, @Valid @RequestBody UpdateUserRoleParam param) {
        userRoleService.updateUserRole(id, param);
    }

    @GetMapping
    public List<UserRole> listByUserId(@NotBlank(message = "用户id不能为空") String userId) {
        return userRoleService.listByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        userRoleService.deleteById(id);
    }

    @DeleteMapping
    public void deleteUserRole(@Valid @RequestBody DeleteUserRoleParam param) {
        userRoleService.deleteUserRole(param);
    }
}
