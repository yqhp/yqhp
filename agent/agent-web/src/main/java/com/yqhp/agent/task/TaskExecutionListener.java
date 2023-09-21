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

import com.yqhp.console.model.vo.Task;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;

/**
 * @author jiangyitao
 */
public interface TaskExecutionListener {
    default void onTaskStarted(Task task) {
    }

    default void onTaskFinished(Task task) {
    }

    default void onLoadPluginStarted(PluginExecutionRecord record) {
    }

    default void onLoadPluginSucceed(PluginExecutionRecord record) {
    }

    default void onLoadPluginFailed(PluginExecutionRecord record, Throwable cause) {
    }

    default void onLoadPluginSkipped(PluginExecutionRecord record) {
    }

    default  void onEvalActionsStarted(Task task) {

    }

    default void onEvalDocStarted(DocExecutionRecord record) {
    }

    default void onEvalDocSucceed(DocExecutionRecord record) {
    }

    default void onEvalDocFailed(DocExecutionRecord record, Throwable cause) {
    }

    default void onEvalDocSkipped(DocExecutionRecord record) {
    }
}
