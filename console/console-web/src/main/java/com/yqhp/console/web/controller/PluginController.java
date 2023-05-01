package com.yqhp.console.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yqhp.console.model.param.CreatePluginParam;
import com.yqhp.console.model.param.UpdatePluginParam;
import com.yqhp.console.model.param.query.PluginPageQuery;
import com.yqhp.console.repository.entity.Plugin;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import com.yqhp.console.web.service.PluginService;
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
@RequestMapping("/plugin")
public class PluginController {

    @Autowired
    private PluginService pluginService;

    @GetMapping
    public List<Plugin> listByProjectId(@NotBlank(message = "项目不能为空") String projectId) {
        return pluginService.listByProjectId(projectId);
    }

    @GetMapping("/details")
    public List<PluginDTO> listDTOByProjectId(@NotBlank(message = "项目不能为空") String projectId) {
        return pluginService.listDTOByProjectId(projectId);
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/all")
    public List<Plugin> listAll() {
        return pluginService.list();
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/page")
    public IPage<Plugin> pageBy(PluginPageQuery query) {
        return pluginService.pageBy(query);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping
    public Plugin createPlugin(@Valid @RequestBody CreatePluginParam createPluginParam) {
        return pluginService.createPlugin(createPluginParam);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        pluginService.deleteById(id);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}")
    public Plugin updatePlugin(@PathVariable String id, @Valid @RequestBody UpdatePluginParam updatePluginParam) {
        return pluginService.updatePlugin(id, updatePluginParam);
    }

}
