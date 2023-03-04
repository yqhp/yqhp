package com.yqhp.agent.action;

import com.yqhp.console.repository.enums.ActionStepsType;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface ActionExecutionListener {
    void onActionStarted(ActionDTO action, boolean isRoot);

    void onActionSuccessful(ActionDTO action, boolean isRoot);

    void onActionFailed(ActionDTO action, Throwable cause, boolean isRoot);

    void onStepsStarted(ActionDTO action, ActionStepsType stepsType, List<ActionStepDTO> steps, boolean isRoot);

    void onStepsSuccessful(ActionDTO action, ActionStepsType stepsType, List<ActionStepDTO> steps, boolean isRoot);

    void onStepsFailed(ActionDTO action, ActionStepsType stepsType, List<ActionStepDTO> steps, Throwable cause, boolean isRoot);

    void onStepStarted(ActionDTO action, ActionStepsType stepsType, ActionStepDTO step, boolean isRoot);

    void onStepSuccessful(ActionDTO action, ActionStepsType stepsType, ActionStepDTO step, boolean isRoot);

    void onStepFailed(ActionDTO action, ActionStepsType stepsType, ActionStepDTO step, Throwable cause, boolean isRoot);
}
