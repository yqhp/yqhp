package com.yqhp.common.zookeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jiangyitao
 */
@Data
@ConfigurationProperties(prefix = "zk")
public class ZkProperties {

    private String addr;
    private String namespace;
    private String charset = "utf8";
    private int sessionTimeoutMs = 60000;
    private int connectionTimeoutMs = 15000;
    private int maxCloseWaitMs = 1000;
    private String defaultData = "";
    private boolean canBeReadOnly = false;
    private boolean useContainerParentsIfAvailable = true;
    private String threadFactoryClassName;

    private Retry retry = new Retry();
    private Authorization authorization = new Authorization();

    @Data
    public class Retry {
        private int maxSleepTimeMs = 10000;
        private int baseSleepTimeMs = 1000;
        private int maxRetries = 3;
    }

    @Data
    public class Authorization {
        private String scheme = "digest";
        private String auth;
    }
}
