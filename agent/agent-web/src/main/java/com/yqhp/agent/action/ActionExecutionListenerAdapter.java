package com.yqhp.agent.action;

import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;

import java.util.List;

/**
 * @author jiangyitao
 */
public class ActionExecutionListenerAdapter implements ActionExecutionListener {
    @Override
    public void onActionStarted(ActionDTO action, boolean isRoot) {

    }

    @Override
    public void onActionSuccessful(ActionDTO action, boolean isRoot) {

    }

    @Override
    public void onActionFailed(ActionDTO action, Throwable cause, boolean isRoot) {

    }

    @Override
    public void onStepsStarted(ActionDTO action, List<ActionStepDTO> steps, boolean isRoot) {

    }

    @Override
    public void onStepsSuccessful(ActionDTO action, List<ActionStepDTO> steps, boolean isRoot) {

    }

    @Override
    public void onStepsFailed(ActionDTO action, List<ActionStepDTO> steps, Throwable cause, boolean isRoot) {

    }

    @Override
    public void onStepStarted(ActionDTO action, ActionStepDTO step, boolean isRoot) {

    }

    @Override
    public void onStepSuccessful(ActionDTO action, ActionStepDTO step, boolean isRoot) {

    }

    @Override
    public void onStepFailed(ActionDTO action, ActionStepDTO step, Throwable cause, boolean isRoot) {

    }
}
