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
package com.yqhp.agent.usbmuxd;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSString;
import lombok.Data;
import org.apache.commons.lang3.Validate;

import java.util.Optional;

/**
 * @author jiangyitao
 */
@Data
public class IDevice {

    private String connectionType;
    private String serialNumber;
    private String usbSerialNumber;
    private Long connectionSpeed;
    private Long deviceId; // 递增的id，不能作为唯一标识
    private Long locationId;
    private Long productId;

    public IDevice(NSDictionary properties) {
        Validate.notNull(properties);

        connectionType = Optional.ofNullable((NSString) properties.get("ConnectionType"))
                .map(NSString::toString).orElse(null);
        serialNumber = Optional.ofNullable((NSString) properties.get("SerialNumber"))
                .map(NSString::toString).orElse(null);
        usbSerialNumber = Optional.ofNullable((NSString) properties.get("USBSerialNumber"))
                .map(NSString::toString).orElse(null);

        connectionSpeed = Optional.ofNullable((NSNumber) properties.get("ConnectionSpeed"))
                .map(NSNumber::longValue).orElse(null);
        deviceId = Optional.ofNullable((NSNumber) properties.get("DeviceID"))
                .map(NSNumber::longValue).orElse(null);
        locationId = Optional.ofNullable((NSNumber) properties.get("LocationID"))
                .map(NSNumber::longValue).orElse(null);
        productId = Optional.ofNullable((NSNumber) properties.get("ProductID"))
                .map(NSNumber::longValue).orElse(null);
    }
}
