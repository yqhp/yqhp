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

import com.yqhp.common.web.util.ApplicationContextUtils;

/**
 * 在spring管理的对象内，只需要注入AgentProperties即可获得配置
 * 非spring管理的对象，统一通过这个类获取配置
 *
 * @author jiangyitao
 */
public class Properties {

    public static String getDownloadDir() {
        return ApplicationContextUtils.getProperty("agent.download-dir");
    }

    public static String getScrcpyServerPath() {
        return ApplicationContextUtils.getProperty("agent.android.scrcpy-server-path");
    }

    public static String getScrcpyVersion() {
        return ApplicationContextUtils.getProperty("agent.android.scrcpy-version");
    }

    public static String getAppiumJsPath() {
        return ApplicationContextUtils.getProperty("agent.appium.js-path");
    }
}
