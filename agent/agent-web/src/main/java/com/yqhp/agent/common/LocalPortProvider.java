package com.yqhp.agent.common;

import cn.hutool.core.net.NetUtil;
import org.springframework.util.Assert;

/**
 * @author jiangyitao
 */
public class LocalPortProvider {

    private static final int[] SCRCPY_PORTS = {20000, 20999, 20000};
    private static final int[] APPIUM_SERVICE_PORTS = {21000, 21999, 21000};
    private static final int[] APPIUM_ANDROID_SYSTEM_PORTS = {22000, 22999, 22000};
    private static final int[] APPIUM_IOS_WDA_PORTS = {30000, 30999, 30000};

    public static int getScrcpyAvailablePort() {
        synchronized (SCRCPY_PORTS) {
            return getAvailablePort(SCRCPY_PORTS);
        }
    }

    public static int getAppiumServiceAvailablePort() {
        synchronized (APPIUM_SERVICE_PORTS) {
            return getAvailablePort(APPIUM_SERVICE_PORTS);
        }
    }

    public static int getAppiumAndroidSystemAvailablePort() {
        synchronized (APPIUM_ANDROID_SYSTEM_PORTS) {
            return getAvailablePort(APPIUM_ANDROID_SYSTEM_PORTS);
        }
    }

    public static int getAppiumIOSWdaAvailablePort() {
        synchronized (APPIUM_IOS_WDA_PORTS) {
            return getAvailablePort(APPIUM_IOS_WDA_PORTS);
        }
    }

    /**
     * @param ports [min, max, curr]  min <= curr <= max min>0 max>0 max-min>=999
     * @return
     */
    private static int getAvailablePort(int[] ports) {
        Assert.isTrue(ports != null && ports.length == 3,
                "ports != null && ports.length == 3");
        int min = ports[0];
        int max = ports[1];
        Assert.isTrue(max - min >= 999, "max - min >= 999");
        int curr = ports[2];
        Assert.isTrue(min > 0 && max > 0 && curr >= min && curr <= max,
                "min > 0 && max > 0 && curr >= min && curr <= max");

        int res = -1;
        for (; ; ) {
            boolean available = NetUtil.isUsableLocalPort(curr);
            if (available) {
                res = curr;
            }
            curr++;

            if (curr > max) {
                curr = min;
            }

            if (available) {
                ports[2] = curr;
                return res;
            }
        }
    }
}
