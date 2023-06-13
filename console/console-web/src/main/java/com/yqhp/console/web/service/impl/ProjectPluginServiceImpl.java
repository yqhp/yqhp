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
package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateProjectPluginParam;
import com.yqhp.console.model.param.DeleteProjectPluginParam;
import com.yqhp.console.model.param.UpdateProjectPluginParam;
import com.yqhp.console.repository.entity.ProjectPlugin;
import com.yqhp.console.repository.mapper.ProjectPluginMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.ProjectPluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class ProjectPluginServiceImpl
        extends ServiceImpl<ProjectPluginMapper, ProjectPlugin>
        implements ProjectPluginService {

    @Autowired
    private Snowflake snowflake;

    @Override
    public void createProjectPlugin(CreateProjectPluginParam param) {
        ProjectPlugin projectPlugin = param.convertTo();
        projectPlugin.setId(snowflake.nextIdStr());

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
    public void updateProjectPlugin(String id, UpdateProjectPluginParam param) {
        ProjectPlugin projectPlugin = getProjectPluginById(id);
        param.update(projectPlugin);
        projectPlugin.setUpdateBy(CurrentUser.id());
        projectPlugin.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(projectPlugin)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PROJECT_PLUGIN_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PROJECT_PLUGIN);
        }
    }

    @Override
    public void deleteById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PROJECT_PLUGIN_FAIL);
        }
    }

    @Override
    public void deleteProjectPlugin(DeleteProjectPluginParam param) {
        LambdaQueryWrapper<ProjectPlugin> query = new LambdaQueryWrapper<>();
        query.eq(ProjectPlugin::getProjectId, param.getProjectId());
        query.eq(ProjectPlugin::getPluginId, param.getPluginId());
        if (!remove(query)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PROJECT_PLUGIN_FAIL);
        }
    }

    @Override
    public ProjectPlugin getProjectPluginById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PROJECT_PLUGIN_NOT_FOUND));
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
