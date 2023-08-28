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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreatePluginParam;
import com.yqhp.console.model.param.UpdatePluginParam;
import com.yqhp.console.model.param.query.PluginPageQuery;
import com.yqhp.console.repository.entity.Plugin;
import com.yqhp.console.repository.entity.PluginFile;
import com.yqhp.console.repository.entity.ProjectPlugin;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import com.yqhp.console.repository.mapper.PluginMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.PluginFileService;
import com.yqhp.console.web.service.PluginService;
import com.yqhp.console.web.service.ProjectPluginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class PluginServiceImpl extends ServiceImpl<PluginMapper, Plugin> implements PluginService {

    @Autowired
    private Snowflake snowflake;
    @Autowired
    private ProjectPluginService projectPluginService;
    @Autowired
    private PluginFileService pluginFileService;

    @Override
    public IPage<Plugin> pageBy(PluginPageQuery query) {
        LambdaQueryWrapper<Plugin> q = new LambdaQueryWrapper<>();
        String keyword = query.getKeyword();
        q.and(StringUtils.hasText(keyword), c -> c
                .like(Plugin::getId, keyword)
                .or()
                .like(Plugin::getName, keyword)
                .or()
                .like(Plugin::getDescription, keyword)
        );
        q.orderByDesc(Plugin::getId);
        return page(new Page<>(query.getPageNumb(), query.getPageSize()), q);
    }

    @Override
    public Plugin createPlugin(CreatePluginParam createPluginParam) {
        Plugin plugin = createPluginParam.convertTo();
        plugin.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        plugin.setCreateBy(currUid);
        plugin.setUpdateBy(currUid);

        try {
            if (!save(plugin)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PLUGIN_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLUGIN);
        }

        return getById(plugin.getId());
    }

    @Override
    public Plugin updatePlugin(String id, UpdatePluginParam updatePluginParam) {
        Plugin plugin = getPluginById(id);
        updatePluginParam.update(plugin);
        plugin.setUpdateBy(CurrentUser.id());
        plugin.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(plugin)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLUGIN_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLUGIN);
        }

        return getById(plugin.getId());
    }

    @Override
    public Plugin getPluginById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PLUGIN_NOT_FOUND));
    }

    @Override
    public void deleteById(String id) {
        List<ProjectPlugin> projectPlugins = projectPluginService.listByPluginId(id);
        if (!projectPlugins.isEmpty()) {
            String projectIds = projectPlugins.stream()
                    .map(ProjectPlugin::getProjectId)
                    .collect(Collectors.joining(","));
            String msg = "项目[" + projectIds + "]，正在使用此插件，无法删除";
            throw new ServiceException(ResponseCodeEnum.PROJECT_IN_USE, msg);
        }
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PLUGIN_FAILED);
        }
    }

    @Override
    public List<Plugin> listByProjectId(String projectId) {
        List<String> pluginIds = projectPluginService.listPluginIdByProjectId(projectId);
        return pluginIds.isEmpty() ? new ArrayList<>() : listByIds(pluginIds);
    }

    @Override
    public List<PluginDTO> listDTOByProjectId(String projectId) {
        List<Plugin> plugins = listByProjectId(projectId);
        return toPluginDTOs(plugins);
    }

    private List<PluginDTO> toPluginDTOs(List<Plugin> plugins) {
        if (CollectionUtils.isEmpty(plugins)) return new ArrayList<>();

        List<String> pluginIds = plugins.stream().map(Plugin::getId).collect(Collectors.toList());
        // pluginId -> files
        Map<String, List<PluginFile>> pluginFilesMap = pluginFileService.listInPluginIds(pluginIds).stream()
                .collect(Collectors.groupingBy(PluginFile::getPluginId));
        return plugins.stream().map(plugin -> {
            PluginDTO dto = new PluginDTO();
            BeanUtils.copyProperties(plugin, dto);
            dto.setFiles(pluginFilesMap.get(plugin.getId()));
            return dto;
        }).collect(Collectors.toList());
    }


}
