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

import lombok.Data;

/**
 * @author jiangyitao
 * <p>
 * curl http://xxx/json
 * [ {
 * "description": "{\"attached\":true,\"empty\":false,\"height\":2062,\"never_attached\":false,\"screenX\":0,\"screenY\":234,\"visible\":false,\"width\":1080}",
 * "devtoolsFrontendUrl": "https://chrome-devtools-frontend.appspot.com/serve_rev/@9407c80213cda69c2b7abcb4fa8e3f74488f4956/inspector.html?ws=localhost:11222/devtools/page/309AF7D7DBE19E86BFCDBF34A8E6C4B8",
 * "id": "309AF7D7DBE19E86BFCDBF34A8E6C4B8",
 * "title": "I am a page title",
 * "type": "page",
 * "url": "file:///android_asset/html/index2.html",
 * "webSocketDebuggerUrl": "ws://localhost:11222/devtools/page/309AF7D7DBE19E86BFCDBF34A8E6C4B8"
 * } ]
 */
@Data
public class Page {
    private String description;
    private String devtoolsFrontendUrl;
    private String id;
    private String title;
    private String type;
    private String url;
    private String webSocketDebuggerUrl;
}
