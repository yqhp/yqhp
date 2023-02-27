package com.yqhp.console.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yqhp.console.repository.enums.PlanExecutionRecordStatus;
import com.yqhp.console.repository.typehandler.DocsTypeHandler;
import com.yqhp.console.repository.typehandler.PluginsTypeHandler;
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
public class PlanExecutionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private String projectId;

    private String planId;

    private String planName;

    @TableField(typeHandler = PluginsTypeHandler.class)
    private List<Plugin> plugins;

    @TableField(typeHandler = DocsTypeHandler.class)
    private List<Doc> docs;

    private PlanExecutionRecordStatus status;

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
