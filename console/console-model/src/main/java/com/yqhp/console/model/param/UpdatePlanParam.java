package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.Plan;
import com.yqhp.console.repository.enums.RunMode;
import com.yqhp.console.repository.jsonfield.PlanAction;
import com.yqhp.console.repository.jsonfield.PlanDevice;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class UpdatePlanParam implements InputConverter<Plan> {

    @NotBlank(message = "名称不能为空")
    @Size(max = 128, message = "名称长度不能超过{max}")
    private String name;

    @Size(max = 256, message = "描述长度不能超过{max}")
    private String description;

    @Valid
    @NotNull(message = "devices不能为null，可以是[]")
    private List<PlanDevice> devices;

    @Valid
    @NotNull(message = "actions不能为null，可以是[]")
    private List<PlanAction> actions;

    private RunMode runMode;
}
