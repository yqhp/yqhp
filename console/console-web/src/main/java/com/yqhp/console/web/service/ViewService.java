package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreateViewParam;
import com.yqhp.console.model.param.UpdateViewParam;
import com.yqhp.console.repository.entity.View;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface ViewService extends IService<View> {
    View createView(CreateViewParam createViewParam);

    void deleteById(String id);

    View updateView(String id, UpdateViewParam updateViewParam);

    View getViewById(String id);

    List<View> listByDocId(String docId);
}
