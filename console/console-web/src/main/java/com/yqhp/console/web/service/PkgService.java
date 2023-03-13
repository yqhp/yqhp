package com.yqhp.console.web.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePkgParam;
import com.yqhp.console.model.param.TreeNodeMoveEvent;
import com.yqhp.console.model.param.UpdatePkgParam;
import com.yqhp.console.model.param.query.PkgTreeQuery;
import com.yqhp.console.repository.entity.Pkg;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface PkgService extends IService<Pkg> {
    Pkg createPkg(CreatePkgParam createPkgParam);

    void deletePkgById(String id);

    Pkg updatePkg(String id, UpdatePkgParam updatePkgParam);

    void move(TreeNodeMoveEvent moveEvent);

    Pkg getPkgById(String id);

    List<Tree<String>> treeBy(PkgTreeQuery query);
}
