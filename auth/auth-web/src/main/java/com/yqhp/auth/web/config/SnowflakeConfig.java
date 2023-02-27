package com.yqhp.auth.web.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiangyitao
 */
@Configuration
public class SnowflakeConfig {
    @Bean
    public Snowflake snowflake() {
        // 有需要再提取到配置文件
        long workerId = 2L;
        long dataCenterId = 2L;
        return IdUtil.createSnowflake(workerId, dataCenterId);
    }
}
