package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.ProjectPluginParam;
import com.yqhp.console.repository.entity.ProjectPlugin;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface ProjectPluginService extends IService<ProjectPlugin> {
    void createProjectPlugin(ProjectPluginParam param);

    void deleteProjectPlugin(ProjectPluginParam param);

    List<ProjectPlugin> listByProjectId(String projectId);

    List<String> listPluginIdByProjectId(String projectId);

    List<ProjectPlugin> listByPluginId(String pluginId);
}
