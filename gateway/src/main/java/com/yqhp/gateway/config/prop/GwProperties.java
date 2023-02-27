package com.yqhp.gateway.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jiangyitao
 */
@Data
@ConfigurationProperties(prefix = "gw")
public class GwProperties {
    private String hello;
}
