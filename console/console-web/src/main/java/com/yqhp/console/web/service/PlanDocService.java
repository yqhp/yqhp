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
import com.yqhp.console.model.param.CreatePlanDocParam;
import com.yqhp.console.model.param.TableRowMoveEvent;
import com.yqhp.console.model.param.UpdatePlanDocParam;
import com.yqhp.console.repository.entity.PlanDoc;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface PlanDocService extends IService<PlanDoc> {

    PlanDoc createPlanDoc(CreatePlanDocParam param);

    List<PlanDoc> createPlanDocs(List<CreatePlanDocParam> params);

    PlanDoc updatePlanDoc(String id, UpdatePlanDocParam param);

    void deleteById(String id);

    void deleteByDocId(String docId);

    PlanDoc getPlanDocById(String id);

    List<String> listEnabledAndSortedDocIdByPlanId(String planId);

    List<PlanDoc> listSortedByPlanId(String planId);

    void move(TableRowMoveEvent moveEvent);
}
