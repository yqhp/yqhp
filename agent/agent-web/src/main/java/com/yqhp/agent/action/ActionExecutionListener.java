package com.yqhp.agent.action;

import com.yqhp.console.repository.jsonfield.ActionStepX;
import com.yqhp.console.repository.jsonfield.ActionX;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface ActionExecutionListener {
    void onActionStarted(ActionX action, boolean isRoot);

    void onActionSuccessful(ActionX action, boolean isRoot);

    void onActionFailed(ActionX action, Throwable cause, boolean isRoot);

    void onStepsStarted(ActionX action, List<ActionStepX> steps, boolean isRoot);

    void onStepsSuccessful(ActionX action, List<ActionStepX> steps, boolean isRoot);

    void onStepsFailed(ActionX action, List<ActionStepX> steps, Throwable cause, boolean isRoot);

    void onStepStarted(ActionX action, ActionStepX step, boolean isRoot);

    void onStepSuccessful(ActionX action, ActionStepX step, boolean isRoot);

    void onStepFailed(ActionX action, ActionStepX step, Throwable cause, boolean isRoot);
}
