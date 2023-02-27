package com.yqhp.agent.web.config;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.yqhp.agent.appium.AppiumServer;
import com.yqhp.agent.web.config.prop.AgentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author jiangyitao
 * com.alibaba.cloud.nacos.discovery#nacosWatch
 */
@Slf4j
@Configuration
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureBefore({SimpleDiscoveryClientAutoConfiguration.class, CommonsClientAutoConfiguration.class})
public class NacosMetadataConfig {

    @Autowired
    private AgentProperties agentProperties;

    @Bean
    public NacosWatch nacosWatch(NacosServiceManager nacosServiceManager,
                                 NacosDiscoveryProperties nacosDiscoveryProperties) {
        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();

        metadata.put("java.version", System.getProperty("java.version", "unknown"));
        metadata.put("os.name", System.getProperty("os.name", "unknown"));
        metadata.put("os.arch", System.getProperty("os.arch", "unknown"));
        metadata.put("os.version", System.getProperty("os.version", "unknown"));
        metadata.put("agent.version", agentProperties.getVersion());
        String appiumJs = agentProperties.getAppium().getJsPath();
        String appiumVersion = AppiumServer.version(appiumJs);
        metadata.put("appium.version", appiumVersion != null ? appiumVersion : "unknown");

        return new NacosWatch(nacosServiceManager, nacosDiscoveryProperties);
    }


}
