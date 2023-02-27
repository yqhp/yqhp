package com.yqhp.agent.web.config;

import com.yqhp.agent.web.config.prop.AgentProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author jiangyitao
 */
@Configuration
@EnableConfigurationProperties({AgentProperties.class})
public class AgentConfig {

}
