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
package com.yqhp.agent.web.service.impl;

import com.yqhp.agent.driver.Driver;
import com.yqhp.agent.web.kafka.MessageProducer;
import com.yqhp.agent.web.service.TaskService;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.kafka.message.DocExecutionRecordMessage;
import com.yqhp.common.kafka.message.PluginExecutionRecordMessage;
import com.yqhp.console.model.vo.Task;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import com.yqhp.console.repository.enums.DocFlow;
import com.yqhp.console.repository.enums.ExecutionStatus;
import com.yqhp.console.repository.jsonfield.DocExecutionLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private MessageProducer producer;

    @Override
    public void execute(Driver driver, Task task) {
        boolean skip = false;

        for (PluginExecutionRecord record : task.getPluginExecutionRecords()) {
            if (skip) {
                skipPlugin(record);
            } else {
                // 加载插件失败, skip -> true
                skip = !loadPluginQuietly(driver, record);
            }
        }

        for (DocExecutionRecord record : task.getDocExecutionRecords()) {
            if (skip) {
                skipDoc(record);
            } else {
                if (!evalDocQuietly(driver, record)
                        && DocFlow.STOP_RUNNING_NEXT_IF_ERROR.equals(record.getDoc().getFlow())) {
                    // 执行doc失败 且 流程为失败终止
                    skip = true;
                }
            }
        }
    }

    private void skipPlugin(PluginExecutionRecord record) {
        PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.SKIPPED);
        producer.sendPluginExecutionRecordMessage(message);
    }

    private boolean loadPluginQuietly(Driver driver, PluginExecutionRecord record) {
        try {
            onLoadPluginStarted(record);
            driver.jshellLoadPlugin(record.getPlugin());
            onLoadPluginSuccessful(record);
            return true;
        } catch (Throwable cause) {
            onLoadPluginFailed(record, cause);
            return false;
        }
    }

    private void onLoadPluginStarted(PluginExecutionRecord record) {
        PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.STARTED);
        message.setStartTime(System.currentTimeMillis());
        producer.sendPluginExecutionRecordMessage(message);
    }

    private void onLoadPluginSuccessful(PluginExecutionRecord record) {
        PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.SUCCESSFUL);
        message.setEndTime(System.currentTimeMillis());
        producer.sendPluginExecutionRecordMessage(message);
    }

    private void onLoadPluginFailed(PluginExecutionRecord record, Throwable cause) {
        PluginExecutionRecordMessage message = new PluginExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.FAILED);
        message.setEndTime(System.currentTimeMillis());
        producer.sendPluginExecutionRecordMessage(message);
        log.error("Load plugin={} failed", record.getPlugin().getName(), cause);
    }

    private void skipDoc(DocExecutionRecord record) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.SKIPPED);
        producer.sendDocExecutionRecordMessage(message);
    }

    private boolean evalDocQuietly(Driver driver, DocExecutionRecord record) {
        try {
            onEvalDocStarted(record);
            List<JShellEvalResult> results = driver.jshellAnalysisAndEval(record.getDoc().getContent());
            boolean failed = results.stream().anyMatch(JShellEvalResult::isFailed);
            if (failed) {
                onEvalDocFailed(record, results, driver.getLogs(), null);
                return false;
            } else {
                onEvalDocSuccessful(record, results, driver.getLogs());
                return true;
            }
        } catch (Throwable cause) {
            onEvalDocFailed(record, null, driver.getLogs(), cause);
            return false;
        } finally {
            driver.clearLogs();
        }
    }

    private void onEvalDocStarted(DocExecutionRecord record) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.STARTED);
        message.setStartTime(System.currentTimeMillis());
        producer.sendDocExecutionRecordMessage(message);
    }

    private void onEvalDocSuccessful(DocExecutionRecord record, List<JShellEvalResult> results, List<DocExecutionLog> logs) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.SUCCESSFUL);
        message.setEndTime(System.currentTimeMillis());
        message.setResults(results);
        message.setLogs(logs);
        producer.sendDocExecutionRecordMessage(message);
    }

    private void onEvalDocFailed(DocExecutionRecord record, List<JShellEvalResult> results, List<DocExecutionLog> logs, Throwable cause) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.FAILED);
        message.setEndTime(System.currentTimeMillis());
        message.setResults(results);
        message.setLogs(logs);
        producer.sendDocExecutionRecordMessage(message);
        // 目前还没遇到过cause != null的情况，在此记录下
        if (cause != null) {
            log.error("Unexpected err, recordId={}", record.getId(), cause);
        }
    }

}
