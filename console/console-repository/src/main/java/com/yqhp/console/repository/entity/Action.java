package com.yqhp.console.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yqhp.console.repository.enums.ActionStatus;
import com.yqhp.console.repository.jsonfield.ActionStep;
import com.yqhp.console.repository.typehandler.ActionStepsTypeHandler;
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
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private String projectId;

    private String pkgId;

    private Integer weight;

    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * before是mysql关键字，加上`before`才不会报错
     * https://baomidou.com/guide/typehandler.html
     */
    @TableField(value = "`before`", typeHandler = ActionStepsTypeHandler.class)
    private List<ActionStep> before;

    /**
     * https://baomidou.com/guide/typehandler.html
     */
    @TableField(typeHandler = ActionStepsTypeHandler.class)
    private List<ActionStep> steps;

    /**
     * after是mysql关键字，加上`after`才不会报错
     * https://baomidou.com/guide/typehandler.html
     */
    @TableField(value = "`after`", typeHandler = ActionStepsTypeHandler.class)
    private List<ActionStep> after;

    private ActionStatus status;

    private Integer flags;

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
