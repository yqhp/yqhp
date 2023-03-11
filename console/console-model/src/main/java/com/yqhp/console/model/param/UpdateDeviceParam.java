package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.Device;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Map;

/**
 * @author jiangyitao
 */
@Data
public class UpdateDeviceParam implements InputConverter<Device> {
    @Size(max = 128, message = "制造商长度不能超过{max}")
    private String manufacturer;
    @Size(max = 128, message = "品牌长度不能超过{max}")
    private String brand;
    @Size(max = 128, message = "型号长度不能超过{max}")
    private String model;
    @Size(max = 128, message = "CPU长度不能超过{max}")
    private String cpu;
    private Long memSize;
    @Size(max = 512, message = "图片url长度不能超过{max}")
    private String imgUrl;
    @Size(max = 16, message = "系统版本长度不能超过{max}")
    private String systemVersion;
    private Integer screenWidth;
    private Integer screenHeight;
    @Size(max = 256, message = "描述长度不能超过{max}")
    private String description;
    private Map<String, Object> extra;
}
