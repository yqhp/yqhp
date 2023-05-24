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
