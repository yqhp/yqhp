package com.yqhp.console.repository.jsonfield;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class PlanAction {
    @NotBlank(message = "actionId不能为空")
    private String actionId;
    private boolean enabled;
}
