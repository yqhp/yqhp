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
