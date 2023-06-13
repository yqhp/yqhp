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

import com.yqhp.auth.model.param.CreateRoleAuthorityParam;
import com.yqhp.auth.model.param.UpdateRoleAuthorityParam;
import com.yqhp.auth.repository.entity.RoleAuthority;
import com.yqhp.auth.web.service.RoleAuthorityService;
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
@Validated
@RestController
@PreAuthorize("hasAuthority('admin')")
@RequestMapping("/roleAuthority")
public class RoleAuthorityController {

    @Autowired
    private RoleAuthorityService roleAuthorityService;

    @PostMapping
    public void createRoleAuthority(@Valid @RequestBody CreateRoleAuthorityParam param) {
        roleAuthorityService.createRoleAuthority(param);
    }

    @PutMapping("/{id}")
    public void updateRoleAuthority(@PathVariable String id, @Valid @RequestBody UpdateRoleAuthorityParam param) {
        roleAuthorityService.updateRoleAuthority(id, param);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        roleAuthorityService.deleteById(id);
    }

    @GetMapping
    public List<RoleAuthority> listByRoleId(@NotBlank(message = "角色id不能为空") String roleId) {
        return roleAuthorityService.listByRoleId(roleId);
    }
}
