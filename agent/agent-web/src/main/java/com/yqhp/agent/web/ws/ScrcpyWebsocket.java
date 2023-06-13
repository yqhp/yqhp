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
package com.yqhp.agent.web.ws;

import com.yqhp.agent.web.ws.message.handler.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @author jiangyitao
 */
@Slf4j
@Controller
@ServerEndpoint(value = "/device/token/{token}/scrcpy")
public class ScrcpyWebsocket extends DeviceWebsocket {

    @OnOpen
    @Override
    public void onOpen(@PathParam("token") String token, Session session) {
        super.onOpen(token, session);
        messageHandler
                .addInputHandler(new StartScrcpyHandler(session, deviceDriver))
                .addInputHandler(new ScrcpyKeyHandler(session, deviceDriver))
                .addInputHandler(new ScrcpyTextHandler(session, deviceDriver))
                .addInputHandler(new ScrcpyTouchHandler(session, deviceDriver))
                .addInputHandler(new ScrcpyScrollHandler(session, deviceDriver));
    }

    @OnClose
    @Override
    public void onClose(Session session) {
        super.onClose(session);
        if (token != null) deviceService.unlockDevice(token);
    }

}
