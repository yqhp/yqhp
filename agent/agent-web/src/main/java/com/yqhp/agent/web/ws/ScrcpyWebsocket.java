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
