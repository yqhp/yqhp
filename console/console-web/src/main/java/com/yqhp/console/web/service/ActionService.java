package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.TreeNodeMoveEvent;
import com.yqhp.console.model.param.CreateActionParam;
import com.yqhp.console.model.param.UpdateActionParam;
import com.yqhp.console.repository.entity.Action;
import com.yqhp.console.repository.jsonfield.ActionDTO;

import java.util.Collection;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface ActionService extends IService<Action> {
    Action createAction(CreateActionParam param);

    Action updateAction(String id, UpdateActionParam param);

    void move(TreeNodeMoveEvent moveEvent);

    void deleteActionById(String id);

    Action getActionById(String id);

    Action getAvailableActionById(String id);

    List<Action> listInPkgIds(Collection<String> pkgIds);

    ActionDTO toActionDTO(Action action);

    List<ActionDTO> listAvailableActionDTOByIds(Collection<String> ids);
}
