package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreateActionParam;
import com.yqhp.console.model.param.TreeNodeMoveEvent;
import com.yqhp.console.model.param.UpdateActionParam;
import com.yqhp.console.repository.entity.Action;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.jsonfield.ActionDTO;

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

    List<Action> listByProjectIdAndInPkgIds(String projectId, Collection<String> pkgIds);

    Action getActionById(String id);

    ActionDTO getActionDTOById(String id, Map<String, ActionDTO> actionCache, Map<String, Doc> docCache);

    ActionDTO toActionDTO(Action action);

    List<ActionDTO> listActionDTOByIds(Collection<String> ids);
}
