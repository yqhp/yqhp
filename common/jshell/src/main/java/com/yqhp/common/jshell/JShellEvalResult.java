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
package com.yqhp.common.jshell;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class JShellEvalResult {
    private String source;
    private boolean failed;
    private Long evalStart;
    private Long evalEnd;
    private List<SnippetRecord> snippetRecords = new ArrayList<>();

    @Data
    public static class SnippetRecord {
        private String id;
        private String source;
        private String value;
        private String status;
        private boolean failed;
        private String exceptionMessage;
        private String exceptionStackTrace;
        private List<String> diagnostics = new ArrayList<>();
    }
}
