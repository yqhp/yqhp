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

    @GetMapping("/mine")
    public List<Project> myProjects() {
        return projectService.myProjects();
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/all")
    public List<Project> getAllProjects() {
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
    public void deleteProjectById(@PathVariable String projectId) {
        projectService.deleteProjectById(projectId);
    }

}
