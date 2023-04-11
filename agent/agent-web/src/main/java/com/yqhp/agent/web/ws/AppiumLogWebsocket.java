package com.yqhp.agent.web.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * @author jiangyitao
 */
@Slf4j
@Controller
@ServerEndpoint(value = "/device/token/{token}/appiumLog")
public class AppiumLogWebsocket extends DeviceWebsocket {

    @OnOpen
    @Override
    public void onOpen(@PathParam("token") String token, Session session) {
        super.onOpen(token, session);
        RemoteEndpoint.Basic remote = session.getBasicRemote();
        deviceDriver.receiveAppiumLog(appiumLog -> {
            try {
                remote.sendText(appiumLog);
            } catch (IOException e) {
                log.warn("send {} error, cause:{}", appiumLog, e.getMessage());
            }
        });
    }

    @OnClose
    @Override
    public void onClose(Session session) {
        super.onClose(session);
        if (deviceDriver != null) {
            deviceDriver.stopReceiveAppiumLog();
        }
    }

}
