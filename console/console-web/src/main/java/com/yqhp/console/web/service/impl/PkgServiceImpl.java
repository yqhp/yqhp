package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.TreeNodeExtra;
import com.yqhp.console.model.param.CreatePkgParam;
import com.yqhp.console.model.param.TreeNodeMoveEvent;
import com.yqhp.console.model.param.UpdatePkgParam;
import com.yqhp.console.model.param.query.PkgTreeQuery;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.entity.Pkg;
import com.yqhp.console.repository.enums.PkgType;
import com.yqhp.console.repository.mapper.PkgMapper;
import com.yqhp.console.web.common.ResourceFlags;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.DocService;
import com.yqhp.console.web.service.PkgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class PkgServiceImpl extends ServiceImpl<PkgMapper, Pkg> implements PkgService {

    public static final String ROOT_PID = "0";

    @Autowired
    private DocService docService;
    @Autowired
    private Snowflake snowflake;

    @Override
    public Pkg createPkg(CreatePkgParam createPkgParam) {
        Pkg pkg = createPkgParam.convertTo();
        pkg.setId(snowflake.nextIdStr());

        int maxWeight = getMaxWeightByProjectIdAndType(createPkgParam.getProjectId(), createPkgParam.getType());
        pkg.setWeight(maxWeight + 1);

        String currUid = CurrentUser.id();
        pkg.setCreateBy(currUid);
        pkg.setUpdateBy(currUid);

        try {
            if (!save(pkg)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PKG_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PKG);
        }

        return getById(pkg.getId());
    }

    @Override
    public void deleteById(String id) {
        Pkg pkg = getPkgById(id);
        if (ResourceFlags.undeletable(pkg.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.PKG_UNDELETABLE);
        }

        List<Pkg> pkgs = listByProjectIdAndType(pkg.getProjectId(), pkg.getType());

        // 子孙后代 + 自己
        Set<String> pkgIds = getDescendant(id, pkgs);
        pkgIds.add(id);

        if (PkgType.DOC.equals(pkg.getType())) {
            // 检查目录下是否有文档
            List<Doc> docs = docService.listByProjectIdAndInPkgIds(pkg.getProjectId(), pkgIds);
            if (!docs.isEmpty()) {
                throw new ServiceException(ResponseCodeEnum.PKG_DOCS_NOT_EMPTY);
            }
        }

        if (!removeByIds(pkgIds)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PKG_FAIL);
        }
    }

    @Override
    public Pkg updatePkg(String id, UpdatePkgParam updatePkgParam) {
        Pkg pkg = getPkgById(id);
        if (ResourceFlags.unupdatable(pkg.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.PKG_UNUPDATABLE);
        }
        boolean renamed = !pkg.getName().equals(updatePkgParam.getName());
        if (renamed && ResourceFlags.unrenamable(pkg.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.PKG_UNRENAMABLE);
        }

        updatePkgParam.update(pkg);
        update(pkg);
        return getById(id);
    }

    @Override
    @Transactional
    public void move(TreeNodeMoveEvent moveEvent) {
        Pkg from = getPkgById(moveEvent.getFrom());
        if (ResourceFlags.unmovable(from.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.PKG_UNMOVABLE);
        }

        // 移动到某个文件夹内
        if (moveEvent.isInner()) {
            from.setParentId(moveEvent.getTo());
            update(from);
            return;
        }

        String currUid = CurrentUser.id();
        Pkg to = getPkgById(moveEvent.getTo());

        Pkg fromPkg = new Pkg();
        fromPkg.setId(from.getId());
        fromPkg.setParentId(to.getParentId());
        fromPkg.setWeight(to.getWeight());
        fromPkg.setUpdateBy(currUid);

        List<Pkg> toUpdatePkgs = new ArrayList<>();
        toUpdatePkgs.add(fromPkg);
        toUpdatePkgs.addAll(
                listByProjectIdAndTypeAndParentIdAndWeightGeOrLe(
                        to.getProjectId(),
                        to.getType(),
                        to.getParentId(),
                        to.getWeight(),
                        moveEvent.isBefore()
                ).stream()
                        .filter(p -> !p.getId().equals(fromPkg.getId()))
                        .map(p -> {
                            Pkg toUpdate = new Pkg();
                            toUpdate.setId(p.getId());
                            toUpdate.setWeight(moveEvent.isBefore() ? p.getWeight() + 1 : p.getWeight() - 1);
                            toUpdate.setUpdateBy(currUid);
                            return toUpdate;
                        }).collect(Collectors.toList())
        );
        try {
            if (!updateBatchById(toUpdatePkgs)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PKG_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PKG);
        }
    }

    private void update(Pkg pkg) {
        pkg.setUpdateBy(CurrentUser.id());
        pkg.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(pkg)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PKG_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PKG);
        }
    }

    @Override
    public Pkg getPkgById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PKG_NOT_FOUND));
    }

    @Override
    public List<Tree<String>> treeBy(PkgTreeQuery query) {
        List<Pkg> pkgs = listByProjectIdAndType(query.getProjectId(), query.getType());
        List<TreeNode<String>> nodes = pkgs.stream()
                .map(pkg -> {
                    TreeNode<String> node = new TreeNode<>(pkg.getId(), pkg.getParentId(), pkg.getName(), pkg.getWeight());
                    node.setExtra(new TreeNodeExtra<>(TreeNodeExtra.Type.PKG, pkg).toMap());
                    return node;
                }).collect(Collectors.toList());

        if (query.isListItem()) {
            List<String> pkgIds = pkgs.stream().map(Pkg::getId).collect(Collectors.toList());
            pkgIds.add(ROOT_PID);
            if (PkgType.DOC.equals(query.getType())) {
                List<Doc> docs = docService.listByProjectIdAndInPkgIds(query.getProjectId(), pkgIds);
                List<TreeNode<String>> docNodes = docs.stream().map(doc -> {
                    TreeNode<String> node = new TreeNode<>(doc.getId(), doc.getPkgId(), doc.getName(), doc.getWeight());
                    doc.setContent(null); // 移除content内容，减少响应体大小
                    node.setExtra(new TreeNodeExtra<>(TreeNodeExtra.Type.DOC, doc).toMap());
                    return node;
                }).collect(Collectors.toList());
                nodes.addAll(docNodes);
            }
        }

        return TreeUtil.build(nodes, query.getParentId());
    }

    /**
     * 获取后代id
     */
    private Set<String> getDescendant(String id, List<Pkg> pkgs) {
        Set<String> descendant = new HashSet<>();
        if (CollectionUtils.isEmpty(pkgs)) {
            return descendant;
        }

        // pkgId -> pkg
        Map<String, Pkg> pkgMap = pkgs.stream()
                .collect(Collectors.toMap(Pkg::getId, Function.identity(), (k1, k2) -> k1));

        for (Pkg pkg : pkgs) {
            if (descendant.contains(pkg.getId())) {
                continue;
            }

            String parentId = pkg.getParentId();
            while (!ROOT_PID.equals(parentId)) {
                if (id.equals(parentId)) {
                    descendant.add(pkg.getId());
                    break;
                }
                if (!pkgMap.containsKey(parentId)) {
                    break;
                }
                parentId = pkgMap.get(parentId).getParentId(); // 往上取
            }
        }

        return descendant;
    }

    private List<Pkg> listByProjectIdAndTypeAndParentIdAndWeightGeOrLe(String projectId, PkgType type, String parentId, Integer weight, boolean ge) {
        LambdaQueryWrapper<Pkg> query = new LambdaQueryWrapper<>();
        query.eq(Pkg::getProjectId, projectId)
                .eq(Pkg::getType, type)
                .eq(Pkg::getParentId, parentId);
        if (ge) {
            query.ge(Pkg::getWeight, weight);
        } else {
            query.le(Pkg::getWeight, weight);
        }
        return list(query);
    }

    private List<Pkg> listByProjectIdAndType(String projectId, PkgType type) {
        LambdaQueryWrapper<Pkg> query = new LambdaQueryWrapper<>();
        query.eq(Pkg::getProjectId, projectId)
                .eq(Pkg::getType, type);
        return list(query);
    }

    private int getMaxWeightByProjectIdAndType(String projectId, PkgType type) {
        return listByProjectIdAndType(projectId, type).stream()
                .mapToInt(Pkg::getWeight)
                .max().orElse(-1);
    }
}
