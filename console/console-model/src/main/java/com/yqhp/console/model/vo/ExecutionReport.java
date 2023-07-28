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
package com.yqhp.console.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yqhp.console.model.dto.DevicesExecutionResult;
import com.yqhp.console.model.dto.ExecutionResult;
import com.yqhp.console.repository.entity.Device;
import com.yqhp.console.repository.entity.Plan;
import com.yqhp.console.repository.entity.Project;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author jiangyitao
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionReport {
    private String id;
    private Project project;
    private Plan plan;
    private String creator;
    private LocalDateTime createTime;

    // 设备
    private Map<String, Device> devices;
    private DevicesExecutionResult devicesResult;

    // 非设备
    private ExecutionResult result;
}
