package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreateDocParam;
import com.yqhp.console.model.param.TreeNodeMoveEvent;
import com.yqhp.console.model.param.UpdateDocParam;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.web.service.DocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author jiangyitao
 */
@Validated
@RestController
@RequestMapping("/doc")
public class DocController {

    @Autowired
    private DocService docService;

    @GetMapping("/{id}")
    public Doc getDocById(@PathVariable String id) {
        return docService.getDocById(id);
    }

    @PostMapping
    public Doc createDoc(@RequestBody @Valid CreateDocParam createDocParam) {
        return docService.createDoc(createDocParam);
    }

    @DeleteMapping("/{id}")
    public void deleteDocById(@PathVariable String id) {
        docService.deleteDocById(id);
    }

    @PutMapping("/{id}")
    public Doc updateDoc(@PathVariable String id, @Valid @RequestBody UpdateDocParam updateDocParam) {
        return docService.updateDoc(id, updateDocParam);
    }

    @PutMapping("/move")
    public void move(@Valid @RequestBody TreeNodeMoveEvent moveEvent) {
        docService.move(moveEvent);
    }
}
