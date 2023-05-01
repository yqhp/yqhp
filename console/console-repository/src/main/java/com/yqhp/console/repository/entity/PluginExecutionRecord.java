package com.yqhp.console.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yqhp.console.repository.enums.ExecutionStatus;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import com.yqhp.console.repository.typehandler.PluginDTOTypeHandler;
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
public class PluginExecutionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private String projectId;

    private String planId;

    private String executionRecordId;

    private String deviceId;

    private String pluginId;

    @TableField(typeHandler = PluginDTOTypeHandler.class)
    private PluginDTO plugin;

    private ExecutionStatus status;

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
