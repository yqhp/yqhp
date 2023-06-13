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
    public void deleteById(@PathVariable String id) {
        pluginFileService.deleteById(id);
    }

}
