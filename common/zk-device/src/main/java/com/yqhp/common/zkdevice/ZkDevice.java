package com.yqhp.common.zkdevice;

import com.yqhp.console.repository.enums.DevicePlatform;
import com.yqhp.console.repository.enums.DeviceType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jiangyitao
 */
@Data
@NoArgsConstructor
public class ZkDevice {

    private String id;
    private DevicePlatform platform;
    private DeviceType type;
    private String location;
    private String model;

    private boolean isLocked;
    private String lockUser;
    private String lockToken;
    private LocalDateTime lockTime;
    private LocalDateTime unlockTime;

    public ZkDevice(String id, DevicePlatform platform, DeviceType type, String location) {
        this.id = id;
        this.platform = platform;
        this.type = type;
        this.location = location;
    }
}
