package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreateProjectPluginParam;
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
    public void deleteProjectPluginById(@PathVariable String id) {
        projectPluginService.deleteProjectPluginById(id);
    }

}
