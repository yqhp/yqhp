package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.ProjectPlugin;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class UpdateProjectPluginParam implements InputConverter<ProjectPlugin> {
    @NotBlank(message = "插件不能为空")
    private String pluginId;
}
