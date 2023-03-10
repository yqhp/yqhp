package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreateViewParam;
import com.yqhp.console.model.param.UpdateViewParam;
import com.yqhp.console.repository.entity.View;
import com.yqhp.console.web.service.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author jiangyitao
 */
@Validated
@RestController
@RequestMapping("/view")
public class ViewController {

    @Autowired
    private ViewService viewService;

    @GetMapping
    public List<View> listByDocId(@NotBlank(message = "文档不能为空") String docId) {
        return viewService.listByDocId(docId);
    }

    @PostMapping
    public void createView(@RequestBody @Valid CreateViewParam createViewParam) {
        viewService.createView(createViewParam);
    }

    @DeleteMapping("/{id}")
    public void deleteViewById(@PathVariable String id) {
        viewService.deleteViewById(id);
    }

    @PutMapping("/{id}")
    public void updateView(@PathVariable String id, @Valid @RequestBody UpdateViewParam updateViewParam) {
        viewService.updateView(id, updateViewParam);
    }

}
