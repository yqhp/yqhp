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
package com.yqhp.common.kafka.message;

/**
 * @author jiangyitao
 */
public class Topics {
    public static final String DOC_EXECUTION_RECORD = "doc-execution-record"; // 40个分区
    public static final String PLUGIN_EXECUTION_RECORD = "plugin-execution-record"; // 40个分区
    public static final String EXECUTION_REPORT = "execution-report"; // 20个分区
}
