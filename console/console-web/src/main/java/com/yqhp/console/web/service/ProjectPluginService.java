package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreateProjectPluginParam;
import com.yqhp.console.model.param.UpdateProjectPluginParam;
import com.yqhp.console.repository.entity.ProjectPlugin;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface ProjectPluginService extends IService<ProjectPlugin> {
    void createProjectPlugin(CreateProjectPluginParam param);

    void updateProjectPlugin(String id, UpdateProjectPluginParam param);

    void deleteProjectPluginById(String id);

    ProjectPlugin getProjectPluginById(String id);

    List<ProjectPlugin> listByProjectId(String projectId);

    List<String> listPluginIdByProjectId(String projectId);

    List<ProjectPlugin> listByPluginId(String pluginId);
}
