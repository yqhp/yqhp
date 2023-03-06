package com.yqhp.console.repository.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.yqhp.console.repository.enums.ActionStepErrorHandler;
import com.yqhp.console.repository.enums.ActionStepFlag;
import com.yqhp.console.repository.enums.ActionStepType;
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
public class ActionStep implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private String projectId;

    private String actionId;

    private ActionStepFlag flag;

    private Integer weight;

    private ActionStepType type;

    private String idOfType;

    private String name;

    private String content;

    private ActionStepErrorHandler errorHandler;

    private Integer enabled;

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
