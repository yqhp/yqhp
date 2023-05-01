package com.yqhp.console.web.controller;

import cn.hutool.core.lang.tree.Tree;
import com.yqhp.console.model.param.CreatePkgParam;
import com.yqhp.console.model.param.TreeNodeMoveEvent;
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

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        pkgService.deleteById(id);
    }

    @PutMapping("/{id}")
    public void updatePkg(@PathVariable String id, @Valid @RequestBody UpdatePkgParam updatePkgParam) {
        pkgService.updatePkg(id, updatePkgParam);
    }

    @PutMapping("/move")
    public void move(@Valid @RequestBody TreeNodeMoveEvent moveEvent) {
        pkgService.move(moveEvent);
    }
}
