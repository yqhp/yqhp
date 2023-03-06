package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.Plugin;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class UpdatePluginParam implements InputConverter<Plugin> {
    @NotBlank(message = "插件名不能为空")
    @Size(max = 128, message = "插件名长度不能超过{max}")
    private String name;
    @Size(max = 256, message = "描述长度不能超过{max}")
    private String description;
}
