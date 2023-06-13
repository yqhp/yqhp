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

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.dto.DeviceDocExecutionResult;
import com.yqhp.console.repository.entity.DocExecutionRecord;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface DocExecutionRecordService extends IService<DocExecutionRecord> {
    List<DocExecutionRecord> listByExecutionRecordId(String executionRecordId);

    List<DocExecutionRecord> listByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId);

    void deleteByExecutionRecordIdAndDeviceId(String executionRecordId, String deviceId);

    DeviceDocExecutionResult statDeviceDocExecutionResult(List<DocExecutionRecord> records);
}
