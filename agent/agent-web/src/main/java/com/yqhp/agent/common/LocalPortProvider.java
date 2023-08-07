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
package com.yqhp.agent.common;

import com.yqhp.common.commons.util.SocketUtils;
import org.springframework.util.Assert;

/**
 * @author jiangyitao
 */
public class LocalPortProvider {

    private static final int[] SCRCPY_PORTS = {20000, 20999, 20000};
    private static final int[] APPIUM_SERVICE_PORTS = {21000, 21999, 21000};
    private static final int[] APPIUM_ANDROID_SYSTEM_PORTS = {22000, 22999, 22000};
    private static final int[] WDA_PORTS = {30000, 30999, 30000};

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

    public static int getWdaAvailablePort() {
        synchronized (WDA_PORTS) {
            return getAvailablePort(WDA_PORTS);
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
            boolean available = SocketUtils.isPortAvailable(curr);
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
