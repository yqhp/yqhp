package com.yqhp.console.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yqhp.console.repository.enums.RunMode;
import com.yqhp.console.repository.jsonfield.PlanAction;
import com.yqhp.console.repository.jsonfield.PlanDevice;
import com.yqhp.console.repository.typehandler.PlanActionsTypeHandler;
import com.yqhp.console.repository.typehandler.PlanDevicesTypeHandler;
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
public class Plan implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private String projectId;

    private String name;

    private String description;

    @TableField(typeHandler = PlanDevicesTypeHandler.class)
    private List<PlanDevice> devices;

    @TableField(typeHandler = PlanActionsTypeHandler.class)
    private List<PlanAction> actions;

    private RunMode runMode;

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
