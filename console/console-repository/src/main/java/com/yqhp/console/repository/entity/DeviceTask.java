package com.yqhp.console.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yqhp.console.repository.enums.DeviceTaskStatus;
import com.yqhp.console.repository.jsonfield.ActionX;
import com.yqhp.console.repository.typehandler.ActionXTypeHandler;
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
@TableName(autoResultMap = true)
public class DeviceTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private String projectId;

    private String planId;

    private String planExecutionRecordId;

    private String deviceId;

    private String actionId;

    @TableField(typeHandler = ActionXTypeHandler.class)
    private ActionX action;

    private DeviceTaskStatus status;

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
