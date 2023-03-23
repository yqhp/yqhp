package com.yqhp.agent.action;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.console.repository.entity.ActionStep;
import com.yqhp.console.repository.enums.ActionStepErrorHandler;
import com.yqhp.console.repository.enums.ActionStepKind;
import com.yqhp.console.repository.enums.ActionStepType;
import com.yqhp.console.repository.jsonfield.ActionDTO;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
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
            Map<ActionStepKind, List<ActionStepDTO>> stepsMap = action.getSteps().stream()
                    .collect(Collectors.groupingBy(ActionStep::getKind));
            execSteps(action, stepsMap.get(ActionStepKind.BEFORE), isRoot);
            try {
                execSteps(action, stepsMap.get(ActionStepKind.NORMAL), isRoot);
            } finally {
                execSteps(action, stepsMap.get(ActionStepKind.AFTER), isRoot);
            }
            listeners.forEach(listener -> listener.onActionSuccessful(action, isRoot));
        } catch (Throwable cause) {
            listeners.forEach(listener -> listener.onActionFailed(action, cause, isRoot));
            throw cause;
        }
    }

    private void execSteps(ActionDTO action, List<ActionStepDTO> steps, boolean isRoot) {
        if (CollectionUtils.isEmpty(steps)) return;

        try {
            listeners.forEach(listener -> listener.onStepsStarted(action, steps, isRoot));
            for (ActionStepDTO step : steps) {
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

    private void execStep(ActionDTO action, ActionStepDTO step, boolean isRoot) {
        if (step == null) return;
        if (!BooleanUtils.toBoolean(step.getEnabled())) {
            return;
        }

        try {
            listeners.forEach(listener -> listener.onStepStarted(action, step, isRoot));
            if (ActionStepType.ACTION.equals(step.getType())) {
                exec(step.getAction(), false);
            } else {
                String content = null;
                if (ActionStepType.DOC.equals(step.getType())) {
                    if (step.getDoc() != null) {
                        content = step.getDoc().getContent();
                    }
                } else if (ActionStepType.JSH.equals(step.getType())) {
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
