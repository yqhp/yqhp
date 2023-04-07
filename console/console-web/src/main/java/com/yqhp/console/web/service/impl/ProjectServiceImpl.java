package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.jshell.JShellConst;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateDocParam;
import com.yqhp.console.model.param.CreatePkgParam;
import com.yqhp.console.model.param.CreateProjectParam;
import com.yqhp.console.model.param.UpdateProjectParam;
import com.yqhp.console.model.param.query.ProjectPageQuery;
import com.yqhp.console.repository.entity.Pkg;
import com.yqhp.console.repository.entity.Project;
import com.yqhp.console.repository.enums.DocKind;
import com.yqhp.console.repository.enums.DocStatus;
import com.yqhp.console.repository.enums.PkgType;
import com.yqhp.console.repository.mapper.ProjectMapper;
import com.yqhp.console.web.common.Const;
import com.yqhp.console.web.common.ResourceFlags;
import com.yqhp.console.web.enums.DefaultPkg;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.DocService;
import com.yqhp.console.web.service.PkgService;
import com.yqhp.console.web.service.ProjectService;
import com.yqhp.console.web.service.UserProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private PkgService pkgService;
    @Autowired
    private DocService docService;
    @Autowired
    private Snowflake snowflake;
    @Autowired
    private UserProjectService userProjectService;

    @Transactional
    @Override
    public Project createProject(CreateProjectParam createProjectParam) {
        Project project = createProjectParam.convertTo();
        project.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        project.setCreateBy(currUid);
        project.setUpdateBy(currUid);

        try {
            if (!save(project)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PROJECT_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PROJECT);
        }

        for (DefaultPkg defaultPkg : DefaultPkg.values()) {
            // 创建默认目录
            CreatePkgParam createPkgParam = new CreatePkgParam();
            createPkgParam.setProjectId(project.getId());
            createPkgParam.setType(PkgType.DOC);
            createPkgParam.setName(defaultPkg.getName());
            createPkgParam.setFlags(ResourceFlags.ALL_LIMITS);
            Pkg pkg = pkgService.createPkg(createPkgParam);

            if (DefaultPkg.INIT.equals(defaultPkg)) {
                CreateDocParam d1 = new CreateDocParam();
                d1.setProjectId(project.getId());
                d1.setPkgId(pkg.getId());
                d1.setKind(DocKind.JSH_DECLARATION);
                d1.setName("默认导入");
                d1.setContent(String.join("\n", JShellConst.DEFAULT_IMPORTS));
                d1.setStatus(DocStatus.RELEASED);
                d1.setFlags(ResourceFlags.UNRENAMABLE | ResourceFlags.UNMOVABLE | ResourceFlags.UNDELETABLE);
                docService.createDoc(d1);

                CreateDocParam d2 = new CreateDocParam();
                d2.setProjectId(project.getId());
                d2.setPkgId(pkg.getId());
                d2.setKind(DocKind.JSH_DECLARATION);
                d2.setName("Appium导入");
                d2.setContent(String.join("\n", Const.APPIUM_IMPORTS));
                d2.setStatus(DocStatus.RELEASED);
                d2.setFlags(ResourceFlags.UNRENAMABLE | ResourceFlags.UNMOVABLE | ResourceFlags.UNDELETABLE);
                docService.createDoc(d2);
            }
        }

        return getById(project.getId());
    }

    @Override
    public void deleteProjectById(String projectId) {
        if (!removeById(projectId)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PROJECT_FAIL);
        }
    }

    @Override
    public Project updateProject(String projectId, UpdateProjectParam updateProjectParam) {
        Project project = getProjectById(projectId);
        updateProjectParam.update(project);
        project.setUpdateBy(CurrentUser.id());
        project.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(project)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PROJECT_FAIL);
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
    public List<Project> myProjects() {
        // admin返回所有项目
        boolean isAdmin = CurrentUser.hasAuthority("admin");
        if (isAdmin) return list();

        List<String> projectIds = userProjectService.
                listProjectIdByUserId(CurrentUser.id());
        return projectIds.isEmpty()
                ? new ArrayList<>()
                : listByIds(projectIds);
    }
}
