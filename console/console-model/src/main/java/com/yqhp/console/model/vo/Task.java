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

import com.yqhp.console.repository.entity.DocExecutionRecord;
import com.yqhp.console.repository.entity.ExecutionRecord;
import com.yqhp.console.repository.entity.PluginExecutionRecord;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class Task {
    private List<PluginExecutionRecord> pluginExecutionRecords;
    private List<DocExecutionRecord> docExecutionRecords;
    private ExecutionRecord executionRecord;
}
