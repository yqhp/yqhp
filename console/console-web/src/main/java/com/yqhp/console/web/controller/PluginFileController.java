package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreatePluginFileParam;
import com.yqhp.console.repository.entity.PluginFile;
import com.yqhp.console.web.service.PluginFileService;
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
@RequestMapping("/pluginFile")
@PreAuthorize("hasAuthority('admin')")
public class PluginFileController {

    @Autowired
    private PluginFileService pluginFileService;

    @PostMapping
    public void createPluginFile(@Valid @RequestBody CreatePluginFileParam param) {
        pluginFileService.createPluginFile(param);
    }

    @GetMapping
    public List<PluginFile> listByPluginId(@NotBlank(message = "pluginId不能为空") String pluginId) {
        return pluginFileService.listByPluginId(pluginId);
    }

    @DeleteMapping("/{id}")
    public void deletePluginFileById(@PathVariable String id) {
        pluginFileService.deletePluginFileById(id);
    }

}
