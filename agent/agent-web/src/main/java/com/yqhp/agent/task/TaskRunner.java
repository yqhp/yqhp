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
package com.yqhp.agent.task;

import com.yqhp.agent.driver.Driver;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.console.model.vo.Task;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.DocFlow;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangyitao
 */
@Slf4j
public class TaskRunner {

    private final List<TaskExecutionListener> listeners = new ArrayList<>();
    private final Driver driver;

    public TaskRunner(Driver driver) {
        this.driver = driver;
    }

    public TaskRunner addListener(TaskExecutionListener listener) {
        listeners.add(listener);
        return this;
    }

    public void runQuietly(Task task) {
        boolean skipped = !onTaskStarted(task);

        for (PluginExecutionRecord record : task.getPluginExecutionRecords()) {
            if (skipped || !onLoadPluginStarted(record)) {
                onLoadPluginSkipped(record);
                continue;
            }
            try {
                driver.jshellLoadPlugin(record.getPlugin());
                onLoadPluginSucceed(record);
            } catch (Throwable cause) {
                skipped = true;
                onLoadPluginFailed(record, cause);
            }
        }

        for (DocExecutionRecord record : task.getDocExecutionRecords()) {
            driver.clearLogs();
            record.setLogs(driver.getLogs());

            if (skipped || !onEvalDocStarted(record)) {
                onEvalDocSkipped(record);
                continue;
            }

            boolean failed;
            Throwable err = null;
            try {
                List<JShellEvalResult> results = driver.jshellAnalysisAndEval(record.getDoc().getContent());
                record.setResults(results);
                failed = results.stream().anyMatch(JShellEvalResult::isFailed);
            } catch (Throwable cause) {
                failed = true;
                err = cause;
            }

            if (failed) {
                onEvalDocFailed(record, err);
                if (DocFlow.STOP_RUNNING_NEXT_IF_ERROR.equals(record.getDoc().getFlow())) {
                    skipped = true;
                }
            } else {
                onEvalDocSucceed(record);
            }
        }

        onTaskFinished(task);
    }

    private boolean onTaskStarted(Task task) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onTaskStarted(task);
            } catch (Throwable cause) {
                log.error("Error onTaskStarted, executionId={}", task.getExecutionRecord().getId(), cause);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean onTaskFinished(Task task) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onTaskFinished(task);
            } catch (Throwable cause) {
                log.error("Error onTaskFinished, executionId={}", task.getExecutionRecord().getId(), cause);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean onLoadPluginSkipped(PluginExecutionRecord record) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onLoadPluginSkipped(record);
            } catch (Throwable cause) {
                log.error("Error onPluginSkipped, recordId={}", record.getId(), cause);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean onLoadPluginStarted(PluginExecutionRecord record) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onLoadPluginStarted(record);
            } catch (Throwable cause) {
                log.error("Error onLoadPluginStarted, recordId={}", record.getId(), cause);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean onLoadPluginSucceed(PluginExecutionRecord record) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onLoadPluginSucceed(record);
            } catch (Throwable cause) {
                log.error("Error onLoadPluginSucceed, recordId={}", record.getId(), cause);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean onLoadPluginFailed(PluginExecutionRecord record, Throwable cause) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onLoadPluginFailed(record, cause);
            } catch (Throwable t) {
                log.error("Error onLoadPluginFailed, recordId={}", record.getId(), t);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean onEvalDocSkipped(DocExecutionRecord record) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onEvalDocSkipped(record);
            } catch (Throwable cause) {
                log.error("Error onEvalDocSkipped, recordId={}", record.getId(), cause);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean onEvalDocStarted(DocExecutionRecord record) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onEvalDocStarted(record);
            } catch (Throwable cause) {
                log.error("Error onEvalDocStarted, recordId={}", record.getId(), cause);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean onEvalDocSucceed(DocExecutionRecord record) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onEvalDocSucceed(record);
            } catch (Throwable cause) {
                log.error("Error onEvalDocSucceed, recordId={}", record.getId(), cause);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean onEvalDocFailed(DocExecutionRecord record, Throwable cause) {
        boolean succeed = true;
        for (TaskExecutionListener listener : getListeners()) {
            try {
                listener.onEvalDocFailed(record, cause);
            } catch (Throwable t) {
                log.error("Error onEvalDocFailed, recordId={}", record.getId(), t);
                succeed = false;
            }
        }
        return succeed;
    }

    private boolean driverListenerAdded = false;

    private List<TaskExecutionListener> getListeners() {
        if (driverListenerAdded) {
            return listeners;
        }

        TaskExecutionListener listener = driver.getTaskExecutionListener();
        if (listener != null) {
            // 优先执行从driver设置的listener
            listeners.add(0, listener);
            driverListenerAdded = true;
        }
        return listeners;
    }

}
