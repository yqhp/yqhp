/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
    public List<View> listByDocId(@NotBlank(message = "docId不能为空") String docId) {
        return viewService.listByDocId(docId);
    }

    @PostMapping
    public void createView(@RequestBody @Valid CreateViewParam createViewParam) {
        viewService.createView(createViewParam);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        viewService.deleteById(id);
    }

    @PutMapping("/{id}")
    public void updateView(@PathVariable String id, @Valid @RequestBody UpdateViewParam updateViewParam) {
        viewService.updateView(id, updateViewParam);
    }

}
