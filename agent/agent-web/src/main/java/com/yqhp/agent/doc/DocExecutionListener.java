package com.yqhp.agent.doc;

import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.console.repository.entity.Doc;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface DocExecutionListener {

    default void onStarted(Doc doc) {
    }

    default void onSuccessful(Doc doc, List<JShellEvalResult> results) {
    }

    default void onFailed(Doc doc, List<JShellEvalResult> results, Throwable cause) {

    }
}
