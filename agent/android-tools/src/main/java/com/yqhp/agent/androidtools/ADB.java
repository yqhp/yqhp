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
package com.yqhp.agent.androidtools;

import com.android.ddmlib.AndroidDebugBridge;
import com.yqhp.common.commons.system.OS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangyitao
 */
@Slf4j
public class ADB {

    private static volatile boolean inited = false;

    public static synchronized void init(String adbPath, Duration timeout) {
        if (inited) {
            throw new IllegalStateException("Adb inited");
        }

        if (StringUtils.isEmpty(adbPath)) {
            String androidHome = System.getenv("ANDROID_HOME");
            if (StringUtils.isEmpty(androidHome)) {
                throw new IllegalStateException("AdbPath is empty && SystemEnv ANDROID_HOME is empty");
            }
            adbPath = androidHome + File.separator + "platform-tools" + File.separator
                    + (OS.isWindows() ? "adb.exe" : "adb");
        }

        if (!Files.exists(Paths.get(adbPath))) {
            throw new IllegalStateException(adbPath + " not found");
        }

        if (timeout == null) timeout = Duration.ofMinutes(2);
        long timeoutMs = timeout.toMillis();

        AndroidDebugBridge.init(false);
        AndroidDebugBridge adb = AndroidDebugBridge
                .createBridge(adbPath, false, timeoutMs, TimeUnit.MILLISECONDS);

        long endTimeInMs = System.currentTimeMillis() + timeoutMs;
        while (!adb.hasInitialDeviceList()) {
            if (System.currentTimeMillis() > endTimeInMs) {
                throw new ADBInitTimeoutException(timeout.toString());
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.warn("Interrupted", e);
            }
        }

        log.info("Adb inited");
        inited = true;
    }

    // 在init前调用
    public static synchronized void addDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
        if (inited) {
            throw new IllegalStateException("Adb inited");
        }
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeListener);
    }

    public static synchronized void terminate() {
        if (inited) {
            AndroidDebugBridge.terminate();
            inited = false;
        }
    }
}
