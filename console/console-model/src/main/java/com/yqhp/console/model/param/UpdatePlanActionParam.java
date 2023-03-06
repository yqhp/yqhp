package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.PlanAction;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class UpdatePlanActionParam implements InputConverter<PlanAction> {
    @NotBlank(message = "actionId不能为空")
    private String actionId;
    private Integer weight;
    private Integer enabled;
}
