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

import com.yqhp.auth.model.param.RoleParam;
import com.yqhp.auth.repository.entity.Role;
import com.yqhp.auth.web.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/role")
@PreAuthorize("hasAuthority('admin')")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/all")
    public List<Role> getAllRoles() {
        return roleService.list();
    }

    @PostMapping
    public void createRole(@Valid @RequestBody RoleParam param) {
        roleService.createRole(param);
    }

    @PutMapping("/{id}")
    public void updateRole(@PathVariable String id, @Valid @RequestBody RoleParam param) {
        roleService.updateRole(id, param);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        roleService.deleteById(id);
    }
}
