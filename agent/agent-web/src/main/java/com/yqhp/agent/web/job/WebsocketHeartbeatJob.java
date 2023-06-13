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
package com.yqhp.agent.web.job;

import com.yqhp.agent.web.ws.WebsocketSessionPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author jiangyitao
 */
@Slf4j
@Component
public class WebsocketHeartbeatJob {

    @Scheduled(fixedDelay = 20_000)
    public void sendHeartbeat() {
        for (Session openingSession : WebsocketSessionPool.getOpeningSessions()) {
            try {
                openingSession.getBasicRemote().sendText("pong");
            } catch (IOException e) {
                log.error("[{}]send heartbeat err", openingSession.getId(), e);
            }
        }
    }
}
