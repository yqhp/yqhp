package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.Plan;
import com.yqhp.console.repository.enums.RunMode;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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

    private RunMode runMode;
}
