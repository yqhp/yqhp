package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreateActionParam;
import com.yqhp.console.model.param.TreeNodeMoveEvent;
import com.yqhp.console.model.param.UpdateActionParam;
import com.yqhp.console.repository.entity.Action;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.jsonfield.ActionX;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author jiangyitao
 */
public interface ActionService extends IService<Action> {
    Action createAction(CreateActionParam param);

    Action updateAction(String id, UpdateActionParam param);

    void move(TreeNodeMoveEvent moveEvent);

    void deleteActionById(String id);

    Action getActionById(String id);

    ActionX getActionXById(String id, Map<String, ActionX> actionCache, Map<String, Doc> docCache);

    List<Action> listByProjectIdAndInPkgIds(String projectId, Collection<String> pkgIds);

    ActionX toActionX(Action action);

    List<ActionX> listActionXByIds(Collection<String> ids);
}
