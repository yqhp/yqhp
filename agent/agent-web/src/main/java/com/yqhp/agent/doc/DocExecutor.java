package com.yqhp.agent.doc;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.console.repository.entity.Doc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangyitao
 */
@Slf4j
public class DocExecutor {

    private final DeviceDriver driver;
    private final List<DocExecutionListener> listeners = new ArrayList<>();

    public DocExecutor(DeviceDriver driver) {
        Assert.notNull(driver, "driver cannot be null");
        this.driver = driver;
    }

    public void addListener(DocExecutionListener listener) {
        Assert.notNull(listener, "listener cannot be null");
        listeners.add(listener);
    }

    public void execQuietly(Doc doc) {
        try {
            exec(doc);
        } catch (Throwable ignore) {
        }
    }

    private void exec(Doc doc) {
        if (doc == null) return;
        listeners.forEach(listener -> listener.onStarted(doc));
        try {
            List<JShellEvalResult> results = driver.jshellEval(doc.getContent());
            boolean failed = results.stream().anyMatch(JShellEvalResult::isFailed);
            if (failed) throw new DocExecutionException(results);
            listeners.forEach(listener -> listener.onSuccessful(doc, results));
        } catch (Throwable cause) {
            if (cause instanceof DocExecutionException) {
                listeners.forEach(listener -> listener.onFailed(doc, ((DocExecutionException) cause).getResults(), cause));
            } else {
                listeners.forEach(listener -> listener.onFailed(doc, null, cause));
            }
            throw cause;
        }
    }


}
