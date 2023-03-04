package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

    @Autowired
    private Snowflake snowflake;

    @Override
    public Doc createDoc(CreateDocParam createDocParam) {
        if (AVAILABLE_DOC_STATUS_LIST.contains(createDocParam.getStatus())
                && !StringUtils.hasText(createDocParam.getContent())) {
            throw new ServiceException(ResponseCodeEnum.AVAILABLE_DOC_CONTENT_MUST_HAS_TEXT);
        }

        Doc doc = createDocParam.convertTo();
        doc.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        doc.setCreateBy(currUid);
        doc.setUpdateBy(currUid);

        if (!save(doc)) {
            throw new ServiceException(ResponseCodeEnum.SAVE_DOC_FAIL);
        }

        return getById(doc.getId());
    }

    @Override
    public Doc updateDoc(String id, UpdateDocParam updateDocParam) {
        if (updateDocParam.getContent() != null
                && AVAILABLE_DOC_STATUS_LIST.contains(updateDocParam.getStatus())
                && !StringUtils.hasText(updateDocParam.getContent())) {
            throw new ServiceException(ResponseCodeEnum.AVAILABLE_DOC_CONTENT_MUST_HAS_TEXT);
        }

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
        if (!updateById(doc)) {
            throw new ServiceException(ResponseCodeEnum.UPDATE_DOC_FAIL);
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
}
