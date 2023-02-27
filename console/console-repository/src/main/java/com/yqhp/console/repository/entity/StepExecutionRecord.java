package com.yqhp.console.repository.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.yqhp.console.repository.enums.ActionStepsType;
import com.yqhp.console.repository.enums.StepExecutionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author mybatis-plus generator
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StepExecutionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private String deviceTaskId;

    private String deviceId;

    private String actionId;

    private ActionStepsType actionStepsType;

    private String stepName;

    private StepExecutionStatus status;

    private Long startTime;

    private Long endTime;

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
