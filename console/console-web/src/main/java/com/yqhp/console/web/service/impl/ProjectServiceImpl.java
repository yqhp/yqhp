package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import com.yqhp.auth.model.CurrentUser;
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

import javax.lang.model.element.Modifier;
import java.time.Duration;
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

            // 创建全局变量类
            if (DefaultPkg.COMMON.equals(defaultPkg)) {
                CreateDocParam createDocParam = new CreateDocParam();
                createDocParam.setProjectId(project.getId());
                createDocParam.setPkgId(pkg.getId());
                createDocParam.setKind(DocKind.JSH_DECLARATION);
                createDocParam.setName("G");
                createDocParam.setStatus(DocStatus.RELEASED);
                createDocParam.setFlags(ResourceFlags.UNRENAMABLE | ResourceFlags.UNMOVABLE | ResourceFlags.UNDELETABLE);

                FieldSpec field1 = FieldSpec.builder(String.class, "PROJECT_ID")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", project.getId())
                        .build();
                FieldSpec field2 = FieldSpec.builder(Duration.class, "PO_DURATION")
                        .addJavadoc("PO模式：等待元素出现的超时时间")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(CodeBlock.of("java.time.Duration.ofMillis(2000)"))
                        .build();
                TypeSpec type = TypeSpec.classBuilder("G")
                        .addModifiers(Modifier.PUBLIC)
                        .addField(field1).addField(field2)
                        .build();
                createDocParam.setContent(type.toString());

                docService.createDoc(createDocParam);
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
