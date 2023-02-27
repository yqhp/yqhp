package com.yqhp.auth.web.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author jiangyitao
 */
@Data
@ConfigurationProperties(prefix = "token")
public class TokenProperties {
    private Duration expire;
}
