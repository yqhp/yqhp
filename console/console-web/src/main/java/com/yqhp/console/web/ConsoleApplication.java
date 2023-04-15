package com.yqhp.console.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author jiangyitao
 */
@EnableFeignClients({"com.yqhp.auth.rpc"})
@MapperScan({"com.yqhp.console.repository.mapper"})
@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
public class ConsoleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsoleApplication.class, args);
    }
}
