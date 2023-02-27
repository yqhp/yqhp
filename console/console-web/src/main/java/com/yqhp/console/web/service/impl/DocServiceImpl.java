package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.squareup.javapoet.TypeSpec;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateDocParam;
import com.yqhp.console.model.param.UpdateDocParam;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.enums.DocStatus;
import com.yqhp.console.repository.enums.DocType;
import com.yqhp.console.repository.mapper.DocMapper;
import com.yqhp.console.web.common.ResourceFlags;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.lang.model.element.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class DocServiceImpl extends ServiceImpl<DocMapper, Doc>
        implements DocService {

    private static final List<DocStatus> AVAILABLE_DOC_STATUS_LIST = List.of(
            DocStatus.RELEASED, DocStatus.DEPRECATED
    );

    private static final Formatter GOOGLE_JAVA_FORMATTER = new Formatter(
            JavaFormatterOptions.builder().style(JavaFormatterOptions.Style.AOSP).build()
    );

    @Autowired
    private Snowflake snowflake;

    @Override
    public Doc createDoc(CreateDocParam createDocParam) {
        Doc doc = createDocParam.convertTo();
        doc.setId(snowflake.nextIdStr());

        if (DocType.JAVA.equals(createDocParam.getType())) {
            if (!StringUtils.hasText(createDocParam.getContent())) {
                String defaultJavaCode = createJavaCodeByName(doc.getName());
                doc.setContent(defaultJavaCode);
            }
            if (AVAILABLE_DOC_STATUS_LIST.contains(createDocParam.getStatus())) {
                doc.setContent(formatJavaCode(doc.getContent()));
            }
        }

        if (AVAILABLE_DOC_STATUS_LIST.contains(createDocParam.getStatus())
                && !StringUtils.hasText(createDocParam.getContent())) {
            throw new ServiceException(ResponseCodeEnum.AVAILABLE_DOC_CONTENT_MUST_HAS_TEXT);
        }

        String currUid = CurrentUser.id();
        doc.setCreateBy(currUid);
        doc.setUpdateBy(currUid);

        try {
            if (!save(doc)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_DOC_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_DOC);
        }

        return getById(doc.getId());
    }

    @Override
    public Doc updateDoc(String id, UpdateDocParam updateDocParam) {
        Doc doc = getDocById(id);
        if (ResourceFlags.unupdatable(doc.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.DOC_UNUPDATABLE);
        }
        boolean renamed = !doc.getName().equals(updateDocParam.getName());
        if (renamed && ResourceFlags.unrenamable(doc.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.DOC_UNRENAMABLE);
        }
        boolean moved = updateDocParam.getPkgId() != null
                && !doc.getPkgId().equals(updateDocParam.getPkgId());
        if (moved && ResourceFlags.unmovable(doc.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.DOC_UNMOVABLE);
        }

        if (updateDocParam.getContent() != null
                && AVAILABLE_DOC_STATUS_LIST.contains(updateDocParam.getStatus())
                && !StringUtils.hasText(updateDocParam.getContent())) {
            throw new ServiceException(ResponseCodeEnum.AVAILABLE_DOC_CONTENT_MUST_HAS_TEXT);
        }

        boolean needJavaFormat = DocType.JAVA.equals(doc.getType())
                && StringUtils.hasText(updateDocParam.getContent())
                && AVAILABLE_DOC_STATUS_LIST.contains(updateDocParam.getStatus())
                && !updateDocParam.getContent().equals(doc.getContent());
        if (needJavaFormat) {
            updateDocParam.setContent(formatJavaCode(updateDocParam.getContent()));
        }
        updateDocParam.update(doc);
        update(doc);
        return getById(id);
    }

    @Override
    public void move(String id, String pkgId) {
        Doc doc = getDocById(id);
        boolean unmoved = doc.getPkgId().equals(pkgId);
        if (unmoved) return;
        if (ResourceFlags.unmovable(doc.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.DOC_UNMOVABLE);
        }

        doc.setPkgId(pkgId);
        update(doc);
    }

    private void update(Doc doc) {
        doc.setUpdateBy(CurrentUser.id());
        doc.setUpdateTime(LocalDateTime.now());
        try {
            if (!updateById(doc)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_DOC_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_DOC);
        }
    }

    @Override
    public void deleteDocById(String id) {
        Doc doc = getDocById(id);
        if (ResourceFlags.undeletable(doc.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.DOC_UNDELETABLE);
        }
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_DOC_FAIL);
        }
    }

    @Override
    public Doc getDocById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.DOC_NOT_FOUND));
    }

    @Override
    public Doc getAvailableDocById(String id) {
        LambdaQueryWrapper<Doc> query = new LambdaQueryWrapper<>();
        query.eq(Doc::getId, id);
        query.in(Doc::getStatus, AVAILABLE_DOC_STATUS_LIST);
        return getOne(query);
    }

    @Override
    public List<Doc> listInPkgIds(Collection<String> pkgIds) {
        if (CollectionUtils.isEmpty(pkgIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Doc> query = new LambdaQueryWrapper<>();
        query.in(Doc::getPkgId, pkgIds);
        return list(query);
    }

    @Override
    public List<Doc> listAvailableDocByProjectIdAndType(String projectId, DocType type) {
        Assert.hasText(projectId, "projectId must has text");
        Assert.notNull(type, "docType cannot be null");

        LambdaQueryWrapper<Doc> query = new LambdaQueryWrapper<>();
        query.eq(Doc::getProjectId, projectId);
        query.eq(Doc::getType, type);
        query.in(Doc::getStatus, AVAILABLE_DOC_STATUS_LIST);
        return list(query);
    }

    private String createJavaCodeByName(String name) {
        TypeSpec type = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC).build();
        return type.toString();
    }

    private String formatJavaCode(String code) {
        if (!StringUtils.hasText(code)) {
            return code;
        }
        try {
            return GOOGLE_JAVA_FORMATTER.formatSource(code);
        } catch (FormatterException e) {
            throw new ServiceException(ResponseCodeEnum.FORMAT_JAVA_CODE_FAIL, "[格式化代码失败]" + e.getMessage());
        }
    }
}
