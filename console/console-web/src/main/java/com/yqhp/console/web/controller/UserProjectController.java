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
package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreateUserProjectParam;
import com.yqhp.console.model.param.DeleteUserProjectParam;
import com.yqhp.console.model.param.UpdateUserProjectParam;
import com.yqhp.console.repository.entity.UserProject;
import com.yqhp.console.web.service.UserProjectService;
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
@RequestMapping("/userProject")
@Validated
@PreAuthorize("hasAuthority('admin')")
public class UserProjectController {

    @Autowired
    private UserProjectService userProjectService;

    @PostMapping
    public void createUserProject(@Valid @RequestBody CreateUserProjectParam param) {
        userProjectService.createUserProject(param);
    }

    @PutMapping("/{id}")
    public void updateUserProject(@PathVariable String id, @Valid @RequestBody UpdateUserProjectParam param) {
        userProjectService.updateUserProject(id, param);
    }

    @GetMapping
    public List<UserProject> listByUserId(@NotBlank(message = "用户id不能为空") String userId) {
        return userProjectService.listByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        userProjectService.deleteById(id);
    }

    @DeleteMapping
    public void deleteUserProject(@Valid @RequestBody DeleteUserProjectParam param) {
        userProjectService.deleteUserProject(param);
    }
}
