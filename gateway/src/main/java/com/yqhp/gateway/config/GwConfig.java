package com.yqhp.gateway.config;

import com.yqhp.gateway.config.prop.GwProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiangyitao
 */
@Configuration
@EnableConfigurationProperties({GwProperties.class})
public class GwConfig {

}
