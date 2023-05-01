package com.yqhp.console.model.vo;

import com.yqhp.common.web.model.OutputConverter;
import com.yqhp.console.repository.entity.PlanDevice;
import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class PlanDeviceVO extends PlanDevice
        implements OutputConverter<PlanDeviceVO, PlanDevice> {
    private DeviceVO device;
}
