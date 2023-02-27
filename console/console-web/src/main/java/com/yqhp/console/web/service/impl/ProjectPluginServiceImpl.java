package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.ProjectPluginParam;
import com.yqhp.console.repository.entity.ProjectPlugin;
import com.yqhp.console.repository.mapper.ProjectPluginMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.ProjectPluginService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class ProjectPluginServiceImpl
        extends ServiceImpl<ProjectPluginMapper, ProjectPlugin>
        implements ProjectPluginService {
    @Override
    public void createProjectPlugin(ProjectPluginParam param) {
        ProjectPlugin projectPlugin = param.convertTo();

        String currUid = CurrentUser.id();
        projectPlugin.setCreateBy(currUid);
        projectPlugin.setUpdateBy(currUid);

        try {
            if (!save(projectPlugin)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PROJECT_PLUGIN_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PROJECT_PLUGIN);
        }
    }

    @Override
    public void deleteProjectPlugin(ProjectPluginParam param) {
        LambdaQueryWrapper<ProjectPlugin> query = new LambdaQueryWrapper<>();
        query.eq(ProjectPlugin::getProjectId, param.getProjectId())
                .eq(ProjectPlugin::getPluginId, param.getPluginId());
        if (!remove(query)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PROJECT_PLUGIN_FAIL);
        }
    }

    @Override
    public List<ProjectPlugin> listByProjectId(String projectId) {
        Assert.hasText(projectId, "projectId must has text");
        LambdaQueryWrapper<ProjectPlugin> query = new LambdaQueryWrapper<>();
        query.eq(ProjectPlugin::getProjectId, projectId);
        return list(query);
    }

    @Override
    public List<String> listPluginIdByProjectId(String projectId) {
        return listByProjectId(projectId).stream()
                .map(ProjectPlugin::getPluginId).collect(Collectors.toList());
    }

    @Override
    public List<ProjectPlugin> listByPluginId(String pluginId) {
        Assert.hasText(pluginId, "pluginId must has text");
        LambdaQueryWrapper<ProjectPlugin> query = new LambdaQueryWrapper<>();
        query.eq(ProjectPlugin::getPluginId, pluginId);
        return list(query);
    }
}
