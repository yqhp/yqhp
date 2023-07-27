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
package com.yqhp.agent.web.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author jiangyitao
 */
@Data
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {

    private String version;
    private String description;
    private String downloadDir;

    private Android android = new Android();
    private IOS iOS = new IOS();
    private Appium appium = new Appium();
    private Plugin plugin = new Plugin();

    @Data
    public class Android {
        private boolean enabled = false;
        private String scrcpyVersion;
        private String scrcpyServerPath;
        private String adbPath;
    }

    @Data
    public class IOS {

        private RealDevice realDevice = new RealDevice();
        private Emulator emulator = new Emulator();

        @Data
        public class RealDevice {
            private boolean enabled = false;
        }

        @Data
        public class Emulator {
            private boolean enabled = false;
            private Duration scanPeriod;
        }
    }

    @Data
    public class Appium {
        private String jsPath;
    }

    @Data
    public class Plugin {
        private String baseDir;
    }
}
