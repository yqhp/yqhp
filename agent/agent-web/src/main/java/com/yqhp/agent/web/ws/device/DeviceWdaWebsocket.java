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

import com.yqhp.agent.driver.IOSDeviceDriver;
import com.yqhp.agent.web.ws.message.handler.WdaFrameHandler;
import com.yqhp.agent.web.ws.message.handler.WdaTouchHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * @author jiangyitao
 */
@Slf4j
@Controller
@ServerEndpoint(value = "/device/wda/token/{token}")
public class DeviceWdaWebsocket extends DeviceWebsocket {

    @Override
    protected void onOpened(Session session) {
        IOSDeviceDriver driver = (IOSDeviceDriver) deviceDriver;
        messageHandler
                .register(new WdaFrameHandler(session, driver))
                .register(new WdaTouchHandler(driver));
    }

    @Override
    protected void onClosed() {
        if (token != null) deviceService.unlockDevice(token);
    }

}
