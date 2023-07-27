/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
        metadata.put("java.version", System.getProperty("java.version"));
        metadata.put("os.name", System.getProperty("os.name"));
        metadata.put("os.arch", System.getProperty("os.arch"));
        metadata.put("os.version", System.getProperty("os.version"));
        metadata.put("agent.version", agentProperties.getVersion());
        metadata.put("agent.description", agentProperties.getDescription());
        metadata.put("appium.version", AppiumServer.version(agentProperties.getAppium().getJsPath()));
        return new NacosWatch(nacosServiceManager, nacosDiscoveryProperties);
    }


}
