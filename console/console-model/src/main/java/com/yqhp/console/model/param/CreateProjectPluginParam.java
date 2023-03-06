package com.yqhp.console.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateProjectPluginParam extends UpdateProjectPluginParam {
    @NotBlank(message = "项目不能为空")
    private String projectId;
}
