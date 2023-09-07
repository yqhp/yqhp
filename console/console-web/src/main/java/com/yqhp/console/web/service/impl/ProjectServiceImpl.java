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
import com.yqhp.console.model.param.CreateProjectParam;
import com.yqhp.console.model.param.UpdateProjectParam;
import com.yqhp.console.model.param.query.ProjectPageQuery;
import com.yqhp.console.repository.entity.Project;
import com.yqhp.console.repository.mapper.ProjectMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.ProjectService;
import com.yqhp.console.web.service.UserProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author jiangyitao
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Autowired
    private Snowflake snowflake;
    @Autowired
    private UserProjectService userProjectService;

    @Override
    public Project createProject(CreateProjectParam param) {
        Project project = param.convertTo();
        project.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        project.setCreateBy(currUid);
        project.setUpdateBy(currUid);

        try {
            if (!save(project)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PROJECT_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PROJECT);
        }

        return getById(project.getId());
    }

    @Override
    public void deleteById(String projectId) {
        if (!removeById(projectId)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PROJECT_FAILED);
        }
    }

    @Override
    public Project updateProject(String projectId, UpdateProjectParam param) {
        Project project = getProjectById(projectId);
        param.update(project);
        project.setUpdateBy(CurrentUser.id());
        project.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(project)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PROJECT_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PROJECT);
        }

        return getById(projectId);
    }

    @Override
    public IPage<Project> pageBy(ProjectPageQuery query) {
        LambdaQueryWrapper<Project> q = new LambdaQueryWrapper<>();
        String keyword = query.getKeyword();
        q.and(StringUtils.hasText(keyword), c -> c
                .like(Project::getId, keyword)
                .or()
                .like(Project::getName, keyword)
        );
        q.orderByDesc(Project::getId);
        return page(new Page<>(query.getPageNumb(), query.getPageSize()), q);
    }

    @Override
    public Project getProjectById(String projectId) {
        return Optional.ofNullable(getById(projectId))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PROJECT_NOT_FOUND));
    }

    @Override
    public List<Project> listJoined() {
        // admin返回所有项目
        if (CurrentUser.isAdmin()) {
            return list();
        }

        List<String> projectIds = userProjectService.listProjectIdByUserId(CurrentUser.id());
        return projectIds.isEmpty() ? new ArrayList<>() : listByIds(projectIds);
    }
}
