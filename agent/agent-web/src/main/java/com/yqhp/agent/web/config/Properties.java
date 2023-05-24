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
