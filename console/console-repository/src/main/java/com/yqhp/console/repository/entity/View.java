package com.yqhp.console.repository.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.yqhp.console.repository.enums.ScreenOrientation;
import com.yqhp.console.repository.enums.ViewType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 视图
 * </p>
 *
 * @author mybatis-plus generator
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class View implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private String docId;

    /**
     * 设备id
     */
    private String deviceId;

    private ViewType type;

    private String source;

    private String imgUrl;

    /**
     * 图片高
     */
    private Integer imgHeight;

    /**
     * 图片宽
     */
    private Integer imgWidth;

    private ScreenOrientation screenOrientation;

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
