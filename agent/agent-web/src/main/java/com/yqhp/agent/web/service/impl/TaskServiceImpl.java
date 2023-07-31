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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jiangyitao
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private MessageProducer producer;

    @Override
    public void execute(Driver driver, Task task) {
        // 加载插件
        for (PluginExecutionRecord record : task.getPluginExecutionRecords()) {
            boolean ok = loadPluginQuietly(driver, record);
            if (!ok) {
                // 插件加载失败，停止运行
                return;
            }
        }
        // 执行doc
        for (DocExecutionRecord record : task.getDocExecutionRecords()) {
            boolean ok = evalDocQuietly(driver, record);
            if (!ok && DocFlow.STOP_RUNNING_NEXT_IF_ERROR.equals(record.getDoc().getFlow())) {
                return;
            }
        }
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
    }

    private boolean evalDocQuietly(Driver driver, DocExecutionRecord record) {
        try {
            onEvalDocStarted(record);
            List<JShellEvalResult> results = driver.jshellAnalysisAndEval(record.getDoc().getContent());
            boolean failed = results.stream().anyMatch(JShellEvalResult::isFailed);
            if (failed) {
                onEvalDocFailed(record, results, null);
                return false;
            } else {
                onEvalDocSuccessful(record, results);
                return true;
            }
        } catch (Throwable cause) {
            onEvalDocFailed(record, null, cause);
            return false;
        }
    }

    private void onEvalDocStarted(DocExecutionRecord record) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.STARTED);
        message.setStartTime(System.currentTimeMillis());
        producer.sendDocExecutionRecordMessage(message);
    }

    private void onEvalDocSuccessful(DocExecutionRecord record, List<JShellEvalResult> results) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.SUCCESSFUL);
        message.setEndTime(System.currentTimeMillis());
        message.setResults(results);
        producer.sendDocExecutionRecordMessage(message);
    }

    private void onEvalDocFailed(DocExecutionRecord record, List<JShellEvalResult> results, Throwable cause) {
        DocExecutionRecordMessage message = new DocExecutionRecordMessage();
        message.setId(record.getId());
        message.setStatus(ExecutionStatus.FAILED);
        message.setEndTime(System.currentTimeMillis());
        message.setResults(results);
        producer.sendDocExecutionRecordMessage(message);
    }

}
