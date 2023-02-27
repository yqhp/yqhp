package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.ProjectPluginParam;
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
    public void createProjectPlugin(@Valid @RequestBody ProjectPluginParam param) {
        projectPluginService.createProjectPlugin(param);
    }

    @GetMapping
    public List<ProjectPlugin> listByProjectId(@NotBlank(message = "项目id不能为空") String projectId) {
        return projectPluginService.listByProjectId(projectId);
    }

    @DeleteMapping
    public void deleteProjectPlugin(@Valid @RequestBody ProjectPluginParam param) {
        projectPluginService.deleteProjectPlugin(param);
    }
}
