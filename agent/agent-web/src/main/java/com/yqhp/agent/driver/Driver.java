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

import com.yqhp.agent.jshell.Agent;
import com.yqhp.agent.jshell.Logger;
import com.yqhp.agent.web.service.PluginService;
import com.yqhp.common.jshell.CompletionItem;
import com.yqhp.common.jshell.JShellContext;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.jshell.TriggerSuggestRequest;
import com.yqhp.common.web.util.ApplicationContextUtils;
import com.yqhp.console.repository.jsonfield.DocExecutionLog;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
@Slf4j
public class Driver {

    private static final AtomicInteger THREAD_GROUP_ID = new AtomicInteger();

    private volatile JShellContext jshellContext;
    private ThreadGroup threadGroup;
    @Getter
    private final List<DocExecutionLog> logs = Collections.synchronizedList(new LinkedList<>());
    private final List<Consumer<DocExecutionLog>> logConsumers = new ArrayList<>();

    private final List<DataSource> jdbcDataSources = new ArrayList<>();
    private final List<JdbcTemplate> jdbcTemplates = new ArrayList<>();

    public JShellContext getOrCreateJShellContext() {
        if (jshellContext == null) {
            synchronized (this) {
                if (jshellContext == null) {
                    log.info("Init jshellContext...");
                    jshellContext = new JShellContext();
                    jshellContext.injectVar(new Agent(this));
                    jshellContext.injectVar(new Logger(this));
                    injectVar(jshellContext);
                    log.info("JshellContext inited");
                }
            }
        }
        return jshellContext;
    }

    protected void injectVar(JShellContext jshellCtx) {

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
        Assert.hasText(path, "Path must has text");
        getOrCreateJShellContext().getJShellX().getJShell().addToClasspath(path);
    }

    public synchronized void closeJShellContext() {
        if (jshellContext != null) {
            log.info("Close jshellContext");
            jshellContext.close();
            jshellContext = null;
        }
    }

    public synchronized ThreadGroup getOrCreateThreadGroup() {
        if (threadGroup == null) {
            String threadGroupName = "driver-" + THREAD_GROUP_ID.getAndIncrement();
            log.info("Init threadGroup, name={}", threadGroupName);
            threadGroup = new ThreadGroup(threadGroupName);
            log.info("ThreadGroup inited, name={}", threadGroupName);
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
            log.info("Stop threadGroup, name={}", threadGroup.getName());
            threadGroup.stop();
            threadGroup = null;
        }
    }

    public void log(DocExecutionLog log) {
        for (Consumer<DocExecutionLog> logConsumer : logConsumers) {
            logConsumer.accept(log);
        }
        logs.add(log);
    }

    public void clearLogs() {
        if (!logs.isEmpty()) {
            log.info("Clear Logs");
            logs.clear();
        }
    }

    public void addLogConsumer(Consumer<DocExecutionLog> consumer) {
        logConsumers.add(consumer);
    }

    public void clearLogConsumers() {
        if (!logConsumers.isEmpty()) {
            log.info("Clear logConsumers");
            logConsumers.clear();
        }
    }

    public JdbcTemplate createJdbcTemplate(DataSource ds) {
        Assert.notNull(ds, "DataSource cannot be null");
        jdbcDataSources.add(ds);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplates.add(jdbcTemplate);
        return jdbcTemplate;
    }

    public void releaseJdbc() {
        for (DataSource ds : jdbcDataSources) {
            if (ds instanceof AutoCloseable) {
                try {
                    // 释放连接池全部资源
                    ((AutoCloseable) ds).close();
                } catch (Exception e) {
                    log.error("Close ds failed", e);
                }
            }
        }
        if (!jdbcDataSources.isEmpty()) {
            log.info("Clear jdbcDataSources");
            jdbcDataSources.clear();
        }
        if (!jdbcTemplates.isEmpty()) {
            log.info("Clear jdbcTemplates");
            jdbcTemplates.clear();
        }
    }

    public void release() {
        closeJShellContext();
        stopThreadGroup();
        clearLogs();
        clearLogConsumers();
        releaseJdbc();
    }
}
