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
package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreateProjectPluginParam;
import com.yqhp.console.model.param.DeleteProjectPluginParam;
import com.yqhp.console.model.param.UpdateProjectPluginParam;
import com.yqhp.console.repository.entity.ProjectPlugin;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface ProjectPluginService extends IService<ProjectPlugin> {
    void createProjectPlugin(CreateProjectPluginParam param);

    void updateProjectPlugin(String id, UpdateProjectPluginParam param);

    void deleteById(String id);

    void deleteProjectPlugin(DeleteProjectPluginParam param);

    ProjectPlugin getProjectPluginById(String id);

    List<ProjectPlugin> listByProjectId(String projectId);

    List<String> listPluginIdByProjectId(String projectId);

    List<ProjectPlugin> listByPluginId(String pluginId);
}
