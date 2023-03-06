package com.yqhp.agent.action;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.repository.enums.ActionStepErrorHandler;
import com.yqhp.console.repository.enums.ActionStepFlag;
import com.yqhp.console.repository.enums.ActionStepType;
import com.yqhp.console.repository.jsonfield.ActionStepX;
import com.yqhp.console.repository.jsonfield.ActionX;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void execQuietly(ActionX action) {
        try {
            exec(action);
        } catch (Throwable ignore) {
        }
    }

    public void exec(ActionX action) {
        exec(action, true);
    }

    private void exec(ActionX action, boolean isRoot) {
        if (action == null) return;

        try {
            listeners.forEach(listener -> listener.onActionStarted(action, isRoot));
            Map<ActionStepFlag, List<ActionStepX>> stepsMap = action.getSteps().stream()
                    .collect(Collectors.groupingBy(ActionStep::getFlag));
            execSteps(action, stepsMap.get(ActionStepFlag.BEFORE), isRoot);
            try {
                execSteps(action, stepsMap.get(ActionStepFlag.NORMAL), isRoot);
            } finally {
                execSteps(action, stepsMap.get(ActionStepFlag.AFTER), isRoot);
            }
            listeners.forEach(listener -> listener.onActionSuccessful(action, isRoot));
        } catch (Throwable cause) {
            listeners.forEach(listener -> listener.onActionFailed(action, cause, isRoot));
            throw cause;
        }
    }

    private void execSteps(ActionX action, List<ActionStepX> steps, boolean isRoot) {
        if (CollectionUtils.isEmpty(steps)) return;

        try {
            listeners.forEach(listener -> listener.onStepsStarted(action, steps, isRoot));
            for (ActionStepX step : steps) {
                try {
                    execStep(action, step, isRoot);
                } catch (Throwable cause) {
                    ActionStepErrorHandler errorHandler = step.getErrorHandler();
                    if (errorHandler == null || ActionStepErrorHandler.THROW_ERROR.equals(errorHandler)) {
                        throw cause;
                    }
                }
            }
            listeners.forEach(listener -> listener.onStepsSuccessful(action, steps, isRoot));
        } catch (Throwable cause) {
            listeners.forEach(listener -> listener.onStepsFailed(action, steps, cause, isRoot));
            throw cause;
        }
    }

    private void execStep(ActionX action, ActionStepX step, boolean isRoot) {
        if (step == null) return;

        try {
            listeners.forEach(listener -> listener.onStepStarted(action, step, isRoot));
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
            listeners.forEach(listener -> listener.onStepSuccessful(action, step, isRoot));
        } catch (Throwable cause) {
            listeners.forEach(listener -> listener.onStepFailed(action, step, cause, isRoot));
            throw cause;
        }
    }


}
