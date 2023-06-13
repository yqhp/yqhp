/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
