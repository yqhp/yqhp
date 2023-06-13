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

import com.yqhp.console.model.param.CreateProjectPluginParam;
import com.yqhp.console.model.param.DeleteProjectPluginParam;
import com.yqhp.console.model.param.UpdateProjectPluginParam;
import com.yqhp.console.repository.entity.ProjectPlugin;
import com.yqhp.console.web.service.ProjectPluginService;
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
@RequestMapping("/projectPlugin")
@Validated
@PreAuthorize("hasAuthority('admin')")
public class ProjectPluginController {

    @Autowired
    private ProjectPluginService projectPluginService;

    @PostMapping
    public void createProjectPlugin(@Valid @RequestBody CreateProjectPluginParam param) {
        projectPluginService.createProjectPlugin(param);
    }

    @PutMapping("/{id}")
    public void updateProjectPlugin(@PathVariable String id, @Valid @RequestBody UpdateProjectPluginParam param) {
        projectPluginService.updateProjectPlugin(id, param);
    }

    @GetMapping
    public List<ProjectPlugin> listByProjectId(@NotBlank(message = "项目id不能为空") String projectId) {
        return projectPluginService.listByProjectId(projectId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        projectPluginService.deleteById(id);
    }

    @DeleteMapping
    public void deleteProjectPlugin(@Valid @RequestBody DeleteProjectPluginParam param) {
        projectPluginService.deleteProjectPlugin(param);
    }

}
