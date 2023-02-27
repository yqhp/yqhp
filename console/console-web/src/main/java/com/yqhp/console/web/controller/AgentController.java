package com.yqhp.console.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/agent")
@PreAuthorize("hasAuthority('admin')")
public class AgentController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/all")
    public List<ServiceInstance> getAgentInstances() {
        return discoveryClient.getInstances("agent-service");
    }
}
