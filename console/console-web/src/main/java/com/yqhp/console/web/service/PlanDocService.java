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
