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
package com.yqhp.agent.iostools;

import com.yqhp.common.commons.system.OS;
import com.yqhp.common.commons.system.Terminal;
import com.yqhp.common.commons.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jiangyitao
 */
@Slf4j
public class IOSUtils {

    private static final String LIST_BOOTED_SIMULATOR_CMD = "xcrun simctl list devices |grep Booted";

    /**
     * 获取已启动的模拟器
     *
     * @return
     */
    public static Set<Simulator> listBootedSimulator() {
        try {
            String res = Terminal.execute(LIST_BOOTED_SIMULATOR_CMD);
            if (StringUtils.isNotEmpty(res)) {
                String[] rows = res.split("\\r?\\n");
                return Stream.of(rows).map(row -> {
                    row = row.trim(); // iPhone 11 (9CC9EA0E-86E9-4B08-9E0A-32290F96EC5F) (Booted)
                    int l = row.indexOf('(');
                    int r = row.indexOf(')');
                    String model = row.substring(0, l - 1);
                    String udid = row.substring(l + 1, r);
                    return new Simulator(udid, model);
                }).collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.error("execute '{}' err", LIST_BOOTED_SIMULATOR_CMD, e);
        }

        return new HashSet<>();
    }

    // https://github.com/danielpaulus/go-ios
    // -----------------------以下go-ios--------------------

    private static String GO_IOS_PATH = null;

    public static void setGoIOSPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            GO_IOS_PATH = path;
        }
    }

    private static String getGoIOSPath() {
        return GO_IOS_PATH == null
                ? OS.isWindows() ? "ios.exe" : "ios"
                : GO_IOS_PATH;
    }

    public static Map getDeviceInfo(String udid) {
        String cmd = new StringJoiner(" ")
                .add(getGoIOSPath())
                .add("info")
                .add("--udid=" + udid).toString();
        log.info("[ios][{}]{}", udid, cmd);
        try {
            return JacksonUtils.readValue(Terminal.execute(cmd), Map.class);
        } catch (IOException e) {
            log.error("[ios][{}]io err", udid, e);
            return null;
        }
    }

    public static ShutdownHookProcessDestroyer forward(String udid, int localPort, int remotePort) throws IOException {
        String cmd = new StringJoiner(" ")
                .add(getGoIOSPath())
                .add("forward")
                .add("--udid=" + udid)
                .add(localPort + "")
                .add(remotePort + "").toString();
        log.info("[ios][{}]{}", udid, cmd);
        // ios forward是阻塞式运行的，需要异步运行
        return Terminal.executeAsync(cmd);
    }

    public static ShutdownHookProcessDestroyer runWda(String udid, String bundleId) throws IOException {
        String cmd = new StringJoiner(" ")
                .add(getGoIOSPath())
                .add("runwda")
                .add("--udid=" + udid)
                .add("--bundleid=" + bundleId + ".xctrunner")
                .add("--testrunnerbundleid=" + bundleId + ".xctrunner")
                .add("--xctestconfig=WebDriverAgentRunner.xctest").toString();
        log.info("[ios][{}]{}", udid, cmd);
        // ios runwda是阻塞式运行的，需要异步运行
        return Terminal.executeAsync(cmd);
    }

    public static void installApp(String udid, File app) {
        String cmd = new StringJoiner(" ")
                .add(getGoIOSPath())
                .add("install")
                .add("--udid=" + udid)
                .add("--path=" + app.getAbsolutePath()).toString();
        log.info("[ios][{}]{}", udid, cmd);
        String result;
        try {
            result = Terminal.execute(cmd);
        } catch (Exception e) {
            throw new InstallAppException(e);
        }
        // {"level":"info","msg":"installation successful","time":"2023-08-13T11:05:05+08:00"}
        if (StringUtils.isBlank(result) || !result.contains("installation successful")) {
            throw new InstallAppException(result);
        }
    }

    // -----------------------以上go-ios--------------------
}
