package com.yqhp.agent.driver;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class DeviceInfo {
    /**
     * 品牌
     */
    private String brand;
    /**
     * 制造商
     */
    private String manufacturer;
    /**
     * 内存 GB
     */
    private Double memSize;
    /**
     * 型号
     */
    private String model;
    private String systemVersion;
    private Integer screenWidth;
    private Integer screenHeight;
}
