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
