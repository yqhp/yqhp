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
