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

import com.yqhp.common.commons.model.Size;
import com.yqhp.common.commons.util.HttpUtils;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author jiangyitao
 */
public class WdaUtils {

    public static ShutdownHookProcessDestroyer run(String udid, String bundleId) throws IOException {
        Validate.notBlank(bundleId, "wda bundleId must has text");
        return IOSUtils.runTest(
                udid,
                bundleId + ".xctrunner",
                bundleId + ".xctrunner",
                "WebDriverAgentRunner.xctest"
        );
    }

    public static boolean isRunning(String wdaUrl) {
        return StringUtils.isNotBlank(wdaUrl) && HttpUtils.isUrlAvailable(wdaUrl + "/status");
    }

    public static String createSession(String wdaUrl, Map<String, Object> capabilities) {
        Map resp = HttpUtils.postJSON(
                wdaUrl + "/session",
                Map.of("capabilities", capabilities),
                Map.class
        );
        return (String) resp.get("sessionId");
    }

    public static String getSessionId(String wdaUrl) {
        Map resp = HttpUtils.get(wdaUrl + "/status", Map.class);
        return (String) resp.get("sessionId");
    }

    // https://appium.github.io/appium-xcuitest-driver/4.33/settings/
    public static Map appiumSettings(String wdaUrl, String sessionId, Map<String, Object> settings) {
        String url = wdaUrl + "/session/" + sessionId + "/appium/settings";
        Map allSettings = (Map) HttpUtils.postJSON(url, Map.of("settings", settings), Map.class).get("value");
        return allSettings;
    }

    /**
     * 获取屏幕逻辑分辨率
     */
    public static Size getLogicalScreenSize(String wdaUrl, String sessionId) {
        String url = wdaUrl + "/session/" + sessionId + "/window/size";
        // {"value":{"width":414,"height":736},"sessionId":"2D125BD1-2A7C-4040-AD1A-6A5EDA523836"}
        Map window = (Map) HttpUtils.get(url, Map.class).get("value");
        return new Size((int) window.get("width"), (int) window.get("height"));
    }

    public static void performActions(String wdaUrl, String sessionId, List<Map<String, Object>> actions) {
        String url = wdaUrl + "/session/" + sessionId + "/actions";
        HttpUtils.postJSON(url, Map.of("actions", actions));
    }

    public static boolean isDeviceLocked(String wdaUrl, String sessionId) {
        String url = wdaUrl + "/session/" + sessionId + "/wda/locked";
        return (boolean) HttpUtils.get(url, Map.class).get("value");
    }

    public static void lockDevice(String wdaUrl, String sessionId) {
        String url = wdaUrl + "/session/" + sessionId + "/wda/lock";
        HttpUtils.postJSON(url, null);
    }

    public static void unlockDevice(String wdaUrl, String sessionId) {
        String url = wdaUrl + "/session/" + sessionId + "/wda/unlock";
        HttpUtils.postJSON(url, null);
    }

    public static void pressButton(String wdaUrl, String sessionId, String name) {
        String url = wdaUrl + "/session/" + sessionId + "/wda/pressButton";
        HttpUtils.postJSON(url, Map.of("name", name));
    }
}
