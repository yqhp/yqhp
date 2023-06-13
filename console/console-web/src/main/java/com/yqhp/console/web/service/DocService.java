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

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreateDocParam;
import com.yqhp.console.model.param.TreeNodeMoveEvent;
import com.yqhp.console.model.param.UpdateDocParam;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.enums.DocKind;

import java.util.Collection;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface DocService extends IService<Doc> {
    Doc createDoc(CreateDocParam createDocParam);

    Doc copy(String id);

    Doc updateDoc(String id, UpdateDocParam updateDocParam);

    void updateContent(String id, String content);

    void move(TreeNodeMoveEvent moveEvent);

    void deleteById(String id);

    Doc getDocById(String id);

    List<Doc> listByProjectIdAndInPkgIds(String projectId, Collection<String> pkgIds);

    List<Doc> listPkgTreeSortedAndAvailableByProjectIdAndKind(String projectId, DocKind kind);

    List<Doc> listInIds(Collection<String> ids);

    List<Doc> listAvailableInIds(Collection<String> ids);
}
