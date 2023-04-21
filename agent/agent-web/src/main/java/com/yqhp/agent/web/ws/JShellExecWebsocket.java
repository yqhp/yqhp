package com.yqhp.agent.web.ws;

import com.yqhp.agent.web.ws.message.handler.JShellEvalHandler;
import com.yqhp.agent.web.ws.message.handler.JShellLoadPluginHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @author jiangyitao
 */
@Slf4j
@Controller
@ServerEndpoint(value = "/device/token/{token}/jshellExec")
public class JShellExecWebsocket extends DeviceWebsocket {
    @OnOpen
    @Override
    public void onOpen(@PathParam("token") String token, Session session) {
        super.onOpen(token, session);
        messageHandler
                .addInputHandler(new JShellLoadPluginHandler(session, deviceDriver))
                .addInputHandler(new JShellEvalHandler(session, deviceDriver));
    }
}
