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
package com.yqhp.agent.web.ws.device;

import com.yqhp.common.web.util.WebsocketSessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * @author jiangyitao
 */
@Slf4j
@Controller
@ServerEndpoint(value = "/device/appiumLog/token/{token}")
public class DeviceAppiumLogWebsocket extends DeviceWebsocket {

    @Override
    protected void onOpened(Session session) {
        deviceDriver.receiveAppiumLog(appiumLog -> {
            try {
                WebsocketSessionUtils.sendText(session, appiumLog);
            } catch (Exception e) {
                log.warn("Failed to send log:{}, cause:{}", appiumLog, e.getMessage());
            }
        });
    }

    @Override
    protected void onClosed() {
        if (deviceDriver != null) {
            deviceDriver.stopReceiveAppiumLog();
        }
    }

}
