package com.yqhp.console.repository.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 项目插件
 * </p>
 *
 * @author mybatis-plus generator
 * @since 2023-02-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectPlugin implements Serializable {

    private static final long serialVersionUID = 1L;

    private String projectId;

    private String pluginId;

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
