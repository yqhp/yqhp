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
import com.yqhp.console.model.param.UpdatePkgParam;
import com.yqhp.console.model.param.query.PkgTreeQuery;
import com.yqhp.console.repository.entity.Action;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.entity.Pkg;
import com.yqhp.console.repository.enums.PkgType;
import com.yqhp.console.repository.mapper.PkgMapper;
import com.yqhp.console.web.common.Const;
import com.yqhp.console.web.common.ResourceFlags;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.ActionService;
import com.yqhp.console.web.service.DocService;
import com.yqhp.console.web.service.PkgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
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

    @Autowired
    private DocService docService;
    @Autowired
    private ActionService actionService;
    @Autowired
    private Snowflake snowflake;

    @Override
    public Pkg createPkg(CreatePkgParam createPkgParam) {
        Pkg pkg = createPkgParam.convertTo();
        pkg.setId(snowflake.nextIdStr());

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
    public void deletePkgById(String pkgId) {
        Pkg pkg = getPkgById(pkgId);
        if (ResourceFlags.undeletable(pkg.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.PKG_UNDELETABLE);
        }

        List<Pkg> pkgs = listByProjectIdAndType(pkg.getProjectId(), pkg.getType());

        // 子孙后代 + 自己
        Set<String> pkgIds = getDescendant(pkgId, pkgs);
        pkgIds.add(pkgId);

        if (PkgType.DOC.equals(pkg.getType())) {
            // 检查包下是否有文档
            List<Doc> docs = docService.listInPkgIds(pkgIds);
            if (!docs.isEmpty()) {
                throw new ServiceException(ResponseCodeEnum.PKG_DOCS_NOT_EMPTY);
            }
        } else if (PkgType.ACTION.equals(pkg.getType())) {
            // 检查包下是否有action
            List<Action> actions = actionService.listInPkgIds(pkgIds);
            if (!actions.isEmpty()) {
                throw new ServiceException(ResponseCodeEnum.PKG_ACTIONS_NOT_EMPTY);
            }
        }

        if (!removeByIds(pkgIds)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PKG_FAIL);
        }
    }

    @Override
    public Pkg updatePkg(String pkgId, UpdatePkgParam updatePkgParam) {
        Pkg pkg = getPkgById(pkgId);
        if (ResourceFlags.unupdatable(pkg.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.PKG_UNUPDATABLE);
        }
        boolean renamed = !pkg.getName().equals(updatePkgParam.getName());
        if (renamed && ResourceFlags.unrenamable(pkg.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.PKG_UNRENAMABLE);
        }
        boolean moved = updatePkgParam.getParentId() != null
                && !pkg.getParentId().equals(updatePkgParam.getParentId());
        if (moved && ResourceFlags.unmovable(pkg.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.PKG_UNMOVABLE);
        }

        updatePkgParam.update(pkg);
        update(pkg);
        return getById(pkgId);
    }

    @Override
    public void move(String pkgId, String parentId) {
        Pkg pkg = getPkgById(pkgId);
        boolean unmoved = pkg.getParentId().equals(parentId);
        if (unmoved) return;
        if (ResourceFlags.unmovable(pkg.getFlags())) {
            throw new ServiceException(ResponseCodeEnum.PKG_UNMOVABLE);
        }

        pkg.setParentId(parentId);
        update(pkg);
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
    public Pkg getPkgById(String pkgId) {
        return Optional.ofNullable(getById(pkgId))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PKG_NOT_FOUND));
    }

    @Override
    public List<Tree<String>> treeBy(PkgTreeQuery query) {
        List<Pkg> pkgs = listByProjectIdAndType(query.getProjectId(), query.getType());
        List<TreeNode<String>> nodes = pkgs.stream()
                .map(pkg -> {
                    // weight先用id，后续可能在pkg加个字段用于排序
                    TreeNode<String> node = new TreeNode<>(pkg.getId(), pkg.getParentId(), pkg.getName(), pkg.getId());
                    TreeNodeExtra<Pkg> extra = createTreeNodeExtra(TreeNodeExtra.Type.PKG, pkg.getFlags(), pkg);
                    node.setExtra(extra.toMap());
                    return node;
                }).collect(Collectors.toList());

        if (query.isListItem()) {
            List<String> pkgIds = pkgs.stream().map(Pkg::getId).collect(Collectors.toList());
            pkgIds.add(Const.ROOT_PID);
            if (PkgType.DOC.equals(query.getType())) {
                List<Doc> docs = docService.listInPkgIds(pkgIds);
                List<TreeNode<String>> docNodes = docs.stream().map(doc -> {
                    // weight先用id，后续可能在doc加个字段用于排序
                    TreeNode<String> node = new TreeNode<>(doc.getId(), doc.getPkgId(), doc.getName(), doc.getId());
                    TreeNodeExtra<Doc> extra = createTreeNodeExtra(TreeNodeExtra.Type.DOC, doc.getFlags(), doc);
                    node.setExtra(extra.toMap());
                    return node;
                }).collect(Collectors.toList());
                nodes.addAll(docNodes);
            } else if (PkgType.ACTION.equals(query.getType())) {
                List<Action> actions = actionService.listInPkgIds(pkgIds);
                List<TreeNode<String>> actionNodes = actions.stream().map(action -> {
                    // weight先用id，后续可能在action加个字段用于排序
                    TreeNode<String> node = new TreeNode<>(action.getId(), action.getPkgId(), action.getName(), action.getId());
                    TreeNodeExtra<Action> extra = createTreeNodeExtra(TreeNodeExtra.Type.ACTION, action.getFlags(), action);
                    node.setExtra(extra.toMap());
                    return node;
                }).collect(Collectors.toList());
                nodes.addAll(actionNodes);
            }
        }

        return TreeUtil.build(nodes, query.getParentId());
    }

    private <T> TreeNodeExtra<T> createTreeNodeExtra(TreeNodeExtra.Type type, int flags, T data) {
        TreeNodeExtra<T> extra = new TreeNodeExtra<>();
        extra.setType(type);
        extra.setDeletable(!ResourceFlags.undeletable(flags));
        extra.setRenamable(!ResourceFlags.unrenamable(flags));
        extra.setMovable(!ResourceFlags.unmovable(flags));
        extra.setUpdatable(!ResourceFlags.unupdatable(flags));
        extra.setData(data);
        return extra;
    }

    /**
     * 获取后代id
     */
    private Set<String> getDescendant(String pkgId, List<Pkg> pkgs) {
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
            while (!Const.ROOT_PID.equals(parentId)) {
                if (pkgId.equals(parentId)) {
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

    private List<Pkg> listByProjectIdAndType(String projectId, PkgType type) {
        LambdaQueryWrapper<Pkg> query = new LambdaQueryWrapper<>();
        query.eq(Pkg::getProjectId, projectId)
                .eq(Pkg::getType, type);
        return list(query);
    }
}
