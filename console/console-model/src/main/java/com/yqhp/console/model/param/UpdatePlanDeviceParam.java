package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.PlanDevice;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class UpdatePlanDeviceParam implements InputConverter<PlanDevice> {
    @NotBlank(message = "deviceId不能为空")
    private String deviceId;
    private Integer enabled;
}
