package com.yqhp.agent.appium;

import com.yqhp.common.commons.system.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author jiangyitao
 */
@Slf4j
public class AppiumServer {

    /**
     * @param appiumJS (<=1.4.x appium.js) (>=1.5.x main.js)
     */
    public static String version(String appiumJS) {
        String cmd = StringUtils.isNotBlank(appiumJS)
                ? "node" + appiumJS + " -v"
                : "appium -v";

        String appiumVersion;
        try {
            appiumVersion = Terminal.execute(cmd);
            log.info("AppiumVersion: {}", appiumVersion);
        } catch (IOException e) {
            log.error("exec: {}, io err", cmd, e);
            return null;
        }

        if (StringUtils.isEmpty(appiumVersion)
                || !appiumVersion.matches("\\d+.\\d+.\\d+.*")) {
            return null;
        }

        return appiumVersion;
    }
}
