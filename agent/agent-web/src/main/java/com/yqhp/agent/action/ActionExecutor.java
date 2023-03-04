package com.yqhp.agent.action;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.console.repository.enums.ActionStepErrorHandler;
import com.yqhp.console.repository.enums.ActionStepType;
import com.yqhp.console.repository.enums.ActionStepsType;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangyitao
 */
@Slf4j
public class ActionExecutor {

    private final DeviceDriver driver;
    private final List<ActionExecutionListener> listeners = new ArrayList<>();

    public ActionExecutor(DeviceDriver driver) {
        Assert.notNull(driver, "driver cannot be null");
        this.driver = driver;
    }

    public void addListener(ActionExecutionListener listener) {
        Assert.notNull(listener, "listener cannot be null");
        listeners.add(listener);
    }

    public void execQuietly(ActionDTO action) {
        try {
            exec(action);
        } catch (Throwable ignore) {
        }
    }

    public void exec(ActionDTO action) {
        exec(action, true);
    }

    private void exec(ActionDTO action, boolean isRoot) {
        if (action == null) return;

        try {
            listeners.forEach(listener -> listener.onActionStarted(action, isRoot));
            execSteps(action, ActionStepsType.BEFORE, action.getBefore(), isRoot);
            try {
                execSteps(action, ActionStepsType.STEPS, action.getSteps(), isRoot);
            } finally {
                execSteps(action, ActionStepsType.AFTER, action.getAfter(), isRoot);
            }
            listeners.forEach(listener -> listener.onActionSuccessful(action, isRoot));
        } catch (Throwable cause) {
            listeners.forEach(listener -> listener.onActionFailed(action, cause, isRoot));
            throw cause;
        }
    }

    private void execSteps(ActionDTO action, ActionStepsType type, List<ActionStepDTO> steps, boolean isRoot) {
        if (CollectionUtils.isEmpty(steps)) return;

        try {
            listeners.forEach(listener -> listener.onStepsStarted(action, type, steps, isRoot));
            for (ActionStepDTO step : steps) {
                try {
                    execStep(action, type, step, isRoot);
                } catch (Throwable cause) {
                    ActionStepErrorHandler errorHandler = step.getErrorHandler();
                    if (errorHandler == null || ActionStepErrorHandler.THROW_ERROR.equals(errorHandler)) {
                        throw cause;
                    }
                }
            }
            listeners.forEach(listener -> listener.onStepsSuccessful(action, type, steps, isRoot));
        } catch (Throwable cause) {
            listeners.forEach(listener -> listener.onStepsFailed(action, type, steps, cause, isRoot));
            throw cause;
        }
    }

    private void execStep(ActionDTO action, ActionStepsType type, ActionStepDTO step, boolean isRoot) {
        if (step == null) return;

        try {
            listeners.forEach(listener -> listener.onStepStarted(action, type, step, isRoot));
            if (ActionStepType.ACTION.equals(step.getType())) {
                exec(step.getAction(), false);
            } else {
                String content = null;
                if (ActionStepType.DOC_JSHELL_RUN.equals(step.getType())) {
                    if (step.getDoc() != null) {
                        content = step.getDoc().getContent();
                    }
                } else if (ActionStepType.JSHELL.equals(step.getType())) {
                    content = step.getContent();
                }
                List<JShellEvalResult> results = driver.jshellEval(content);
                boolean stepFailed = results.stream().anyMatch(JShellEvalResult::isFailed);
                if (stepFailed) {
                    throw new ActionStepExecutionException(results);
                }
            }
            listeners.forEach(listener -> listener.onStepSuccessful(action, type, step, isRoot));
        } catch (Throwable cause) {
            listeners.forEach(listener -> listener.onStepFailed(action, type, step, cause, isRoot));
            throw cause;
        }
    }


}
