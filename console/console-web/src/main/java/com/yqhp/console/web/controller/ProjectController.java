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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yqhp.console.model.param.CreateProjectParam;
import com.yqhp.console.model.param.UpdateProjectParam;
import com.yqhp.console.model.param.query.ProjectPageQuery;
import com.yqhp.console.repository.entity.Project;
import com.yqhp.console.web.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/joined")
    public List<Project> listJoined() {
        return projectService.listJoined();
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/all")
    public List<Project> listAll() {
        return projectService.list();
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/page")
    public IPage<Project> pageBy(ProjectPageQuery query) {
        return projectService.pageBy(query);
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/{projectId}")
    public Project getProjectById(@PathVariable String projectId) {
        return projectService.getProjectById(projectId);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping
    public void createProject(@RequestBody @Valid CreateProjectParam createProjectParam) {
        projectService.createProject(createProjectParam);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{projectId}")
    public void updateProject(@PathVariable String projectId, @RequestBody @Valid UpdateProjectParam updateProjectParam) {
        projectService.updateProject(projectId, updateProjectParam);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{projectId}")
    public void deleteById(@PathVariable String projectId) {
        projectService.deleteById(projectId);
    }

}
