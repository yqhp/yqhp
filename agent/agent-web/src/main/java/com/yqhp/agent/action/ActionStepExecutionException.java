package com.yqhp.agent.action;

import com.yqhp.common.jshell.JShellEvalResult;
import lombok.Getter;

import java.util.List;

/**
 * @author jiangyitao
 */
public class ActionStepExecutionException extends RuntimeException {

    @Getter
    private final List<JShellEvalResult> results;

    public ActionStepExecutionException(List<JShellEvalResult> results) {
        this.results = results;
    }
}
