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
package com.yqhp.console.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yqhp.console.model.param.query.ExecutionRecordPageQuery;
import com.yqhp.console.model.vo.ExecutionReport;
import com.yqhp.console.model.vo.Task;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.web.service.ExecutionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author jiangyitao
 */
@Validated
@RestController
@RequestMapping("/executionRecord")
public class ExecutionRecordController {

    @Autowired
    private ExecutionRecordService executionRecordService;

    @PostMapping("/page")
    public IPage<ExecutionRecord> pageBy(@RequestBody @Valid ExecutionRecordPageQuery query) {
        return executionRecordService.pageBy(query);
    }

    @GetMapping("/receive")
    public Task receiveTask(String deviceId) {
        return executionRecordService.receiveTask(deviceId);
    }

    @GetMapping("/{id}/report")
    public ExecutionReport getReportById(@PathVariable String id) {
        return executionRecordService.getReportById(id);
    }

    @DeleteMapping("/{id}/device/{deviceId}")
    public void deleteDeviceExecutionRecord(@PathVariable String id, @PathVariable String deviceId) {
        executionRecordService.deleteDeviceExecutionRecord(id, deviceId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        executionRecordService.deleteById(id);
    }
}
