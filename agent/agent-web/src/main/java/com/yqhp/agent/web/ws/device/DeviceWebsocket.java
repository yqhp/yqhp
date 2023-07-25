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

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.agent.web.ws.BaseWebsocket;
import com.yqhp.agent.web.ws.WebsocketSessionPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;

/**
 * @author jiangyitao
 */
@Slf4j
public class DeviceWebsocket extends BaseWebsocket {

    public static DeviceService deviceService;

    @Autowired
    public void setDeviceService(DeviceService deviceService) {
        DeviceWebsocket.deviceService = deviceService;
    }

    protected DeviceDriver deviceDriver;
    protected String token;

    /**
     * 注意，子类Override需要带上@OnOpen才能生效
     */
    @OnOpen
    public void onOpen(@PathParam("token") String token, Session session) {
        log.info("[{}]onOpen, token:{}", session.getId(), token);
        deviceDriver = deviceService.getDeviceDriverByToken(token); // 检查token，抛出异常进入@OnError
        this.token = token;
        WebsocketSessionPool.addSession(session);
        onOpened(session);
    }

    protected void onOpened(Session session) {

    }
}
