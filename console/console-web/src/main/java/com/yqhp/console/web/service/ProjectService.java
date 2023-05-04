package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreateProjectParam;
import com.yqhp.console.model.param.UpdateProjectParam;
import com.yqhp.console.model.param.query.ProjectPageQuery;
import com.yqhp.console.repository.entity.Project;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface ProjectService extends IService<Project> {
    Project createProject(CreateProjectParam createProjectParam);

    void deleteById(String projectId);

    Project updateProject(String projectId, UpdateProjectParam updateProjectParam);

    IPage<Project> pageBy(ProjectPageQuery query);

    Project getProjectById(String projectId);

    List<Project> listJoined();
}
