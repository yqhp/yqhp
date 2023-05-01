package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.View;
import com.yqhp.console.repository.enums.ScreenOrientation;
import com.yqhp.console.repository.enums.ViewType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class UpdateViewParam implements InputConverter<View> {

    @Size(max = 128, message = "设备id长度不能超过{max}")
    private String deviceId;

    @NotNull(message = "视图类型不能为空")
    private ViewType type;

    private String source;

    @Size(max = 1024, message = "图片url长度不能超过{max}")
    private String imgUrl;
    private Integer imgHeight;
    private Integer imgWidth;

    private ScreenOrientation screenOrientation;
}
