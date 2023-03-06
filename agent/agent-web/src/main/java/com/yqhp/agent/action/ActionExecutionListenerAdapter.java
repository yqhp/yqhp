package com.yqhp.agent.action;

import com.yqhp.console.repository.jsonfield.ActionStepX;
import com.yqhp.console.repository.jsonfield.ActionX;

import java.util.List;

/**
 * @author jiangyitao
 */
public class ActionExecutionListenerAdapter implements ActionExecutionListener {
    @Override
    public void onActionStarted(ActionX action, boolean isRoot) {

    }

    @Override
    public void onActionSuccessful(ActionX action, boolean isRoot) {

    }

    @Override
    public void onActionFailed(ActionX action, Throwable cause, boolean isRoot) {

    }

    @Override
    public void onStepsStarted(ActionX action, List<ActionStepX> steps, boolean isRoot) {

    }

    @Override
    public void onStepsSuccessful(ActionX action, List<ActionStepX> steps, boolean isRoot) {

    }

    @Override
    public void onStepsFailed(ActionX action, List<ActionStepX> steps, Throwable cause, boolean isRoot) {

    }

    @Override
    public void onStepStarted(ActionX action, ActionStepX step, boolean isRoot) {

    }

    @Override
    public void onStepSuccessful(ActionX action, ActionStepX step, boolean isRoot) {

    }

    @Override
    public void onStepFailed(ActionX action, ActionStepX step, Throwable cause, boolean isRoot) {

    }
}
