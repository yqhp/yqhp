package com.yqhp.auth.web.config;

import com.yqhp.auth.web.config.prop.TokenProperties;
import lombok.Data;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiangyitao
 */
@Data
@Configuration
@EnableConfigurationProperties({TokenProperties.class})
public class AuthConfig {

}
