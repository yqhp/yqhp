package com.yqhp.console.web.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePkgParam;
import com.yqhp.console.model.param.UpdatePkgParam;
import com.yqhp.console.model.param.query.PkgTreeQuery;
import com.yqhp.console.repository.entity.Pkg;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface PkgService extends IService<Pkg> {
    Pkg createPkg(CreatePkgParam createPkgParam);

    void deletePkgById(String pkgId);

    Pkg updatePkg(String pkgId, UpdatePkgParam updatePkgParam);

    void move(String pkgId, String parentId);

    Pkg getPkgById(String pkgId);

    List<Tree<String>> treeBy(PkgTreeQuery query);
}
