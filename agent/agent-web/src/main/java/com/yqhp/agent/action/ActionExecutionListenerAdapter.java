package com.yqhp.agent.action;

import com.yqhp.console.repository.enums.ActionStepsType;
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
    public void onStepsStarted(ActionDTO action, ActionStepsType stepsType, List<ActionStepDTO> steps, boolean isRoot) {

    }

    @Override
    public void onStepsSuccessful(ActionDTO action, ActionStepsType stepsType, List<ActionStepDTO> steps, boolean isRoot) {

    }

    @Override
    public void onStepsFailed(ActionDTO action, ActionStepsType stepsType, List<ActionStepDTO> steps, Throwable cause, boolean isRoot) {

    }

    @Override
    public void onStepStarted(ActionDTO action, ActionStepsType stepsType, ActionStepDTO step, boolean isRoot) {

    }

    @Override
    public void onStepSuccessful(ActionDTO action, ActionStepsType stepsType, ActionStepDTO step, boolean isRoot) {

    }

    @Override
    public void onStepFailed(ActionDTO action, ActionStepsType stepsType, ActionStepDTO step, Throwable cause, boolean isRoot) {

    }
}
