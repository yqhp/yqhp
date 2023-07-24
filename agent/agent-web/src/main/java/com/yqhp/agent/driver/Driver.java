/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.agent.driver;

import com.yqhp.agent.jshell.YQHP;
import com.yqhp.agent.web.service.PluginService;
import com.yqhp.common.jshell.CompletionItem;
import com.yqhp.common.jshell.JShellContext;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.jshell.TriggerSuggestRequest;
import com.yqhp.common.web.util.ApplicationContextUtils;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
@Slf4j
public class Driver {

    private static final List<String> DEFAULT_JSHELL_TO_EVAL = List.of(
            "String print(Object o) { return String.valueOf(o); }"
    );

    private static final AtomicInteger THREAD_GROUP_ID = new AtomicInteger();

    private volatile JShellContext jshellContext;
    private volatile ThreadGroup threadGroup;

    public JShellContext getOrCreateJShellContext() {
        if (jshellContext == null) {
            synchronized (this) {
                if (jshellContext == null) {
                    log.info("init jshell context...");
                    jshellContext = new JShellContext();
                    for (String toEval : DEFAULT_JSHELL_TO_EVAL) {
                        log.info("jshell eval: {}", toEval);
                        jshellContext.getJShellX().eval(toEval);
                    }
                    injectVar(jshellContext);
                    log.info("init jshell context completed");
                }
            }
        }
        return jshellContext;
    }

    public void injectVar(JShellContext context) {
        context.injectVar(new YQHP(this));
    }

    public JShellEvalResult jshellEval(String input) {
        return getOrCreateJShellContext().getJShellX().eval(input);
    }

    public JShellEvalResult jshellEval(String input, Consumer<JShellEvalResult> consumer) {
        return getOrCreateJShellContext().getJShellX().eval(input, consumer);
    }

    public List<JShellEvalResult> jshellAnalysisAndEval(String input) {
        return getOrCreateJShellContext().getJShellX().analysisAndEval(input);
    }

    public List<JShellEvalResult> jshellAnalysisAndEval(String input, Consumer<JShellEvalResult> consumer) {
        return getOrCreateJShellContext().getJShellX().analysisAndEval(input, consumer);
    }

    public List<CompletionItem> jshellSuggestions(TriggerSuggestRequest request) {
        return getOrCreateJShellContext().getJShellX().getSuggestions(request);
    }

    private static final PluginService PLUGIN_SERVICE = ApplicationContextUtils.getBean(PluginService.class);

    public List<File> jshellLoadPlugin(PluginDTO plugin) throws IOException {
        List<File> files = PLUGIN_SERVICE.downloadIfAbsent(plugin);
        jshellAddToClasspath(files);
        return files;
    }

    public void jshellAddToClasspath(List<File> files) {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        for (File file : files) {
            jshellAddToClasspath(file.getAbsolutePath());
        }
    }

    public void jshellAddToClasspath(String path) {
        Assert.hasText(path, "path must has text");
        getOrCreateJShellContext().getJShellX().getJShell().addToClasspath(path);
    }

    public synchronized void closeJShellContext() {
        if (jshellContext != null) {
            log.info("close jshell context");
            jshellContext.close();
            jshellContext = null;
        }
    }

    public synchronized ThreadGroup getOrCreateThreadGroup() {
        if (threadGroup == null) {
            String threadGroupName = "driver-" + THREAD_GROUP_ID.getAndIncrement();
            log.info("init thread group, name={}", threadGroupName);
            threadGroup = new ThreadGroup(threadGroupName);
            log.info("init thread group completed, name={}", threadGroupName);
        }
        return threadGroup;
    }

    /**
     * 统一使用该方法执行异步任务。这样的好处是，stopThreadGroup可以立即停止正在执行的任务。避免出现死循环永远无法停止的情况
     */
    public void runAsync(Runnable runnable) {
        Thread thread = new Thread(getOrCreateThreadGroup(), runnable);
        thread.start();
    }

    public synchronized void stopThreadGroup() {
        if (threadGroup != null) {
            log.info("stop thread group, name={}", threadGroup.getName());
            threadGroup.stop();
            threadGroup = null;
        }
    }

    public void release() {
        closeJShellContext();
        stopThreadGroup();
    }
}
