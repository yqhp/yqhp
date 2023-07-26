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
package com.yqhp.console.web.controller;

import com.yqhp.console.model.vo.AgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/instance")
    public List<ServiceInstance> listAgentInstance() {
        return discoveryClient.getInstances("agent-service");
    }

    @GetMapping("/info")
    public List<AgentInfo> listAgentInfo() {
        return listAgentInstance().stream().map(instance -> {
            AgentInfo agentInfo = new AgentInfo();
            agentInfo.setLocation(Base64Utils.encodeToUrlSafeString((instance.getHost() + ":" + instance.getPort()).getBytes()));
            agentInfo.setHost(instance.getHost());
            agentInfo.setPort(instance.getPort());
            agentInfo.setAgentVersion(instance.getMetadata().get("agent.version"));
            agentInfo.setJavaVersion(instance.getMetadata().get("java.version"));
            agentInfo.setOsVersion(instance.getMetadata().get("os.version"));
            agentInfo.setOsName(instance.getMetadata().get("os.name"));
            agentInfo.setOsArch(instance.getMetadata().get("os.arch"));
            return agentInfo;
        }).collect(Collectors.toList());
    }
}
