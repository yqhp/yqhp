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
package com.yqhp.console.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.console.repository.enums.DocKind;
import com.yqhp.console.repository.enums.ExecutionStatus;
import com.yqhp.console.repository.jsonfield.DocExecutionLog;
import com.yqhp.console.repository.typehandler.DocTypeHandler;
import com.yqhp.console.repository.typehandler.DocExecutionLogsTypeHandler;
import com.yqhp.console.repository.typehandler.JShellEvalResultsTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author mybatis-plus generator
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(autoResultMap = true)
public class DocExecutionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private String projectId;

    private String planId;

    private String executionRecordId;

    private String deviceId;

    private String docId;

    private DocKind docKind;

    @TableField(typeHandler = DocTypeHandler.class)
    private Doc doc;

    private ExecutionStatus status;

    private Long startTime;

    private Long endTime;

    @TableField(typeHandler = JShellEvalResultsTypeHandler.class)
    private List<JShellEvalResult> results;

    @TableField(typeHandler = DocExecutionLogsTypeHandler.class)
    private List<DocExecutionLog> logs;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;


}
