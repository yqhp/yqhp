package com.yqhp.agent.doc;

import com.yqhp.common.jshell.JShellEvalResult;
import lombok.Getter;

import java.util.List;

/**
 * @author jiangyitao
 */
public class DocExecutionException extends RuntimeException {

    @Getter
    private final List<JShellEvalResult> results;

    public DocExecutionException(List<JShellEvalResult> results) {
        this.results = results;
    }
}
