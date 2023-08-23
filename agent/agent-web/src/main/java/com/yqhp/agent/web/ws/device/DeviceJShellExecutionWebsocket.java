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

import com.yqhp.agent.web.ws.message.handler.JShellEvalHandler;
import com.yqhp.agent.web.ws.message.handler.JShellLoadPluginHandler;
import com.yqhp.common.commons.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * @author jiangyitao
 */
@Slf4j
@Controller
@ServerEndpoint(value = "/device/jshellExecution/token/{token}")
public class DeviceJShellExecutionWebsocket extends DeviceWebsocket {

    @Override
    protected void onOpened(Session session) {
        messageHandler
                .register(new JShellLoadPluginHandler(session, deviceDriver))
                .register(new JShellEvalHandler(session, deviceDriver));

        RemoteEndpoint.Basic remote = session.getBasicRemote();
        deviceDriver.addLogConsumer(_log -> {
            if (session.isOpen()) {
                try {
                    remote.sendText(JacksonUtils.writeValueAsString(_log));
                } catch (IOException e) {
                    log.warn("ws send error, log:{}, cause:{}", _log, e.getMessage());
                }
            }
        });
    }

}
