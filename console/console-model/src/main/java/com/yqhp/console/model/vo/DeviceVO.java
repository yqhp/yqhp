package com.yqhp.console.model.vo;

import com.yqhp.common.web.model.OutputConverter;
import com.yqhp.console.model.enums.DeviceStatus;
import com.yqhp.console.repository.entity.Device;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author jiangyitao
 */
@Data
public class DeviceVO extends Device implements OutputConverter<DeviceVO, Device> {
    private String location;
    private DeviceStatus status;
    private String lockUser;
    private LocalDateTime lockTime;
}
