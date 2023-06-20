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
package com.yqhp.agent.androidtools.browser;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author jiangyitao
 * <p>
 * curl http://xxx/json/version
 * {
 * "Android-Package": "io.appium.android.apis",
 * "Browser": "Chrome/87.0.4280.101",
 * "Protocol-Version": "1.3",
 * "User-Agent": "Mozilla/5.0 (Linux; Android 10; Redmi Note 7 Pro Build/QKQ1.190915.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/87.0.4280.101 Mobile Safari/537.36",
 * "V8-Version": "8.7.220.29",
 * "WebKit-Version": "537.36 (@9407c80213cda69c2b7abcb4fa8e3f74488f4956)",
 * "webSocketDebuggerUrl": "ws://localhost:11222/devtools/browser"
 * }
 */
@Data
public class Version {
    @JsonProperty("Android-Package")
    private String androidPackage;
    @JsonProperty("Browser")
    private String browser;
    @JsonProperty("Protocol-Version")
    private String protocolVersion;
    @JsonProperty("User-Agent")
    private String userAgent;
    @JsonProperty("V8-Version")
    private String v8Version;
    @JsonProperty("WebKit-Version")
    private String webKitVersion;
    private String webSocketDebuggerUrl;
}
