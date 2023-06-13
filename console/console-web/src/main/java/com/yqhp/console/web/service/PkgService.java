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

    void deleteById(String id);

    Pkg updatePkg(String id, UpdatePkgParam updatePkgParam);

    void move(TreeNodeMoveEvent moveEvent);

    Pkg getPkgById(String id);

    List<Tree<String>> treeBy(PkgTreeQuery query);
}
