package com.yqhp.agent.action;

import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface ActionExecutionListener {
    default void onActionStarted(ActionDTO action, boolean isRoot) {
    }

    default void onActionSuccessful(ActionDTO action, boolean isRoot) {
    }

    default void onActionFailed(ActionDTO action, Throwable cause, boolean isRoot) {
    }

    default void onStepsStarted(ActionDTO action, List<ActionStepDTO> steps, boolean isRoot) {
    }

    default void onStepsSuccessful(ActionDTO action, List<ActionStepDTO> steps, boolean isRoot) {
    }

    default void onStepsFailed(ActionDTO action, List<ActionStepDTO> steps, Throwable cause, boolean isRoot) {
    }

    default void onStepStarted(ActionDTO action, ActionStepDTO step, boolean isRoot) {
    }

    default void onStepSuccessful(ActionDTO action, ActionStepDTO step, List<JShellEvalResult> results, boolean isRoot) {
    }

    default void onStepFailed(ActionDTO action, ActionStepDTO step, List<JShellEvalResult> results, Throwable cause, boolean isRoot) {
    }
}
