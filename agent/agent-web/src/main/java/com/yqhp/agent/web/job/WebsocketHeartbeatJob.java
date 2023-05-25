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
