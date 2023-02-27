package com.yqhp.console.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class CreatePlanParam extends UpdatePlanParam {
    @NotBlank(message = "项目不能为空")
    private String projectId;
}
