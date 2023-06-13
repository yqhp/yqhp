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
