package com.yqhp.console.web.controller;

import cn.hutool.core.lang.tree.Tree;
import com.yqhp.console.model.param.CreatePkgParam;
import com.yqhp.console.model.param.UpdatePkgParam;
import com.yqhp.console.model.param.query.PkgTreeQuery;
import com.yqhp.console.web.service.PkgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author jiangyitao
 */
@Validated
@RestController
@RequestMapping("/pkg")
public class PkgController {

    @Autowired
    private PkgService pkgService;

    @GetMapping("/tree")
    public List<Tree<String>> treeBy(@Valid PkgTreeQuery query) {
        return pkgService.treeBy(query);
    }

    @PostMapping
    public void createPkg(@RequestBody @Valid CreatePkgParam createPkgParam) {
        pkgService.createPkg(createPkgParam);
    }

    @DeleteMapping("/{pkgId}")
    public void deletePkgById(@PathVariable String pkgId) {
        pkgService.deletePkgById(pkgId);
    }

    @PutMapping("/{pkgId}")
    public void updatePkg(@PathVariable String pkgId, @Valid @RequestBody UpdatePkgParam updatePkgParam) {
        pkgService.updatePkg(pkgId, updatePkgParam);
    }

    @PutMapping("/{pkgId}/moveTo/{parentId}")
    public void move(@PathVariable String pkgId, @PathVariable String parentId) {
        pkgService.move(pkgId, parentId);
    }
}
