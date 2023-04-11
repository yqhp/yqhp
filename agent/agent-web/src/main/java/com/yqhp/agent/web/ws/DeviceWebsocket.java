package com.yqhp.agent.web.ws;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.agent.web.ws.message.Output;
import com.yqhp.agent.web.ws.message.OutputSender;
import com.yqhp.agent.web.ws.message.handler.MessageHandler;
import com.yqhp.agent.web.ws.message.handler.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @author jiangyitao
 */
@Slf4j
@Controller // 注意: 每个会话都是一个新实例
@ServerEndpoint(value = "/device/token/{token}")
public class DeviceWebsocket {

    private static DeviceService deviceService;

    @Autowired
    public void setStaticField(DeviceService deviceService) {
        DeviceWebsocket.deviceService = deviceService;
    }

    private String token;
    private MessageHandler messageHandler;

    @OnOpen
    public void onOpen(@PathParam("token") String token, Session session) {
        log.info("[{}]onOpen, token={}", session.getId(), token);
        DeviceDriver deviceDriver = deviceService.getDeviceDriverByToken(token);
        this.token = token;
        messageHandler = new MessageHandler()
                .addInputHandler(new StartScrcpyHandler(session, deviceDriver))
                .addInputHandler(new ScrcpyKeyHandler(session, deviceDriver))
                .addInputHandler(new ScrcpyTextHandler(session, deviceDriver))
                .addInputHandler(new ScrcpyTouchHandler(session, deviceDriver))
                .addInputHandler(new ScrcpyScrollHandler(session, deviceDriver))
                .addInputHandler(new ReceiveDeviceLogHandler(session, deviceDriver))
                .addInputHandler(new StopReceiveDeviceLogHandler(session, deviceDriver))
                .addInputHandler(new ReceiveAppiumLogHandler(session, deviceDriver))
                .addInputHandler(new StopReceiveAppiumLogHandler(session, deviceDriver))
                .addInputHandler(new JShellEvalHandler(session, deviceDriver))
                .addInputHandler(new JShellSuggestionsHandler(session, deviceDriver))
                .addInputHandler(new JShellDocumentationHandler(session, deviceDriver));
    }

    @OnClose
    public void onClose(Session session) {
        log.info("[{}]onClose", session.getId());
        if (token != null) deviceService.unlockDevice(token);
    }

    @OnError
    public void onError(Throwable cause, Session session) {
        log.error("[{}]onError", session.getId(), cause);
        Output<?> output = new Output<>();
        output.setStatus(Output.Status.ERROR);
        output.setMessage(cause.getMessage());
        OutputSender.send(session, output);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        messageHandler.handle(message, ((input, cause) -> {
            Output<?> output = new Output<>();
            output.setStatus(Output.Status.ERROR);
            output.setMessage(cause.getMessage());
            if (input != null) {
                output.setUid(input.getUid());
                output.setCommand(input.getCommand());
            }
            OutputSender.send(session, output);
        }));
    }

}
