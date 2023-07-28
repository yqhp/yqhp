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
package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.dto.DevicesExecutionResult;
import com.yqhp.console.model.dto.ExecutionResult;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.model.vo.ExecutionReport;
import com.yqhp.console.model.vo.Task;
import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface ExecutionRecordService extends IService<ExecutionRecord> {
    void push(String deviceId, String executionRecordId);

    void push(ExecutionRecord executionRecord, List<PluginExecutionRecord> pluginExecutionRecords, List<DocExecutionRecord> docExecutionRecords);

    boolean removePushed(String deviceId, String executionRecordId);

    Task receiveDeviceTask(String deviceId);

    ExecutionRecord getExecutionRecordById(String id);

    List<ExecutionRecord> listUnfinished(LocalDateTime since);

    IPage<ExecutionRecord> pageBy(ExecutionRecordPageQuery query);

    void deleteDeviceExecutionRecord(String id, String deviceId);

    ExecutionReport getReportById(String id);

    ExecutionResult statExecutionResult(ExecutionRecord record);

    DevicesExecutionResult statDevicesExecutionResult(ExecutionRecord record);
}
