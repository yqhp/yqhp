package com.yqhp.console.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class DeleteProjectPluginParam {
    @NotBlank(message = "项目不能为空")
    private String projectId;
    @NotBlank(message = "插件不能为空")
    private String pluginId;
}
