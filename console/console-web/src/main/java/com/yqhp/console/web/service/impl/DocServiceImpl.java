package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateDocParam;
import com.yqhp.console.model.param.TreeNodeMoveEvent;
import com.yqhp.console.model.param.UpdateDocParam;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.enums.DocType;
import com.yqhp.console.repository.mapper.DocMapper;
import com.yqhp.console.web.common.ResourceFlags;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class DocServiceImpl extends ServiceImpl<DocMapper, Doc>
        implements DocService {

    @Autowired
    private Snowflake snowflake;

    @Override
    public Doc createDoc(CreateDocParam createDocParam) {
        Doc doc = createDocParam.convertTo();
        doc.setId(snowflake.nextIdStr());

        Integer minWeight = getMinWeightByProjectId(createDocParam.getProjectId());
        doc.setWeight(minWeight != null ? minWeight - 1 : null);

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

        updateDocParam.update(doc);
        update(doc);
        return getById(id);
    }

    @Override
    @Transactional
    public void move(TreeNodeMoveEvent moveEvent) {
        Doc doc = getDocById(moveEvent.getFrom());
        if (ResourceFlags.unmovable(doc.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.DOC_UNMOVABLE);
        }

        // 移动到某个文件夹内
        if (moveEvent.isInner()) {
            doc.setPkgId(moveEvent.getTo());
            update(doc);
            return;
        }

        String currUid = CurrentUser.id();
        LocalDateTime now = LocalDateTime.now();
        Doc toDoc = getDocById(moveEvent.getTo());

        Doc fromDoc = new Doc();
        fromDoc.setId(doc.getId());
        fromDoc.setPkgId(toDoc.getPkgId());
        fromDoc.setWeight(toDoc.getWeight());
        fromDoc.setUpdateBy(currUid);
        fromDoc.setUpdateTime(now);

        List<Doc> toUpdateDocs = new ArrayList<>();
        toUpdateDocs.add(fromDoc);
        toUpdateDocs.addAll(
                listByProjectIdAndPkgIdAndWeightGeOrLe(
                        toDoc.getProjectId(),
                        toDoc.getPkgId(),
                        toDoc.getWeight(),
                        moveEvent.isBefore()
                ).stream().map(d -> {
                    if (d.getId().equals(fromDoc.getId())) {
                        return null;
                    }
                    Doc toUpdate = new Doc();
                    toUpdate.setId(d.getId());
                    toUpdate.setWeight(moveEvent.isBefore() ? d.getWeight() + 1 : d.getWeight() - 1);
                    toUpdate.setUpdateBy(currUid);
                    toUpdate.setUpdateTime(now);
                    return toUpdate;
                }).filter(Objects::nonNull).collect(Collectors.toList())
        );
        try {
            if (!updateBatchById(toUpdateDocs)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_DOC_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_DOC);
        }
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
    public List<Doc> listByProjectIdAndInPkgIds(String projectId, Collection<String> pkgIds) {
        if (CollectionUtils.isEmpty(pkgIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Doc> query = new LambdaQueryWrapper<>();
        query.eq(Doc::getProjectId, projectId);
        query.in(Doc::getPkgId, pkgIds);
        return list(query);
    }

    @Override
    public List<Doc> listByProjectIdAndType(String projectId, DocType type) {
        Assert.hasText(projectId, "projectId must has text");
        Assert.notNull(type, "docType cannot be null");

        LambdaQueryWrapper<Doc> query = new LambdaQueryWrapper<>();
        query.eq(Doc::getProjectId, projectId);
        query.eq(Doc::getType, type);
        return list(query);
    }

    private List<Doc> listByProjectIdAndPkgIdAndWeightGeOrLe(String projectId, String pkgId, Integer weight, boolean ge) {
        LambdaQueryWrapper<Doc> query = new LambdaQueryWrapper<>();
        query.eq(Doc::getProjectId, projectId)
                .eq(Doc::getPkgId, pkgId);
        if (ge) {
            query.ge(Doc::getWeight, weight);
        } else {
            query.le(Doc::getWeight, weight);
        }
        return list(query);
    }

    private List<Doc> listByProjectId(String projectId) {
        Assert.hasText(projectId, "projectId must has text");
        LambdaQueryWrapper<Doc> query = new LambdaQueryWrapper<>();
        query.eq(Doc::getProjectId, projectId);
        return list(query);
    }

    private Integer getMinWeightByProjectId(String projectId) {
        return listByProjectId(projectId).stream()
                .min(Comparator.comparing(Doc::getWeight))
                .map(Doc::getWeight).orElse(null);
    }
}
