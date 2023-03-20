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
import java.io.IOException;

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

    private Session session;
    private String token;
    private MessageHandler messageHandler;

    @OnOpen
    public void onOpen(@PathParam("token") String token, Session session) throws IOException {
        log.info("[{}]onOpen, token={}", session.getId(), token);
        this.session = session;
        DeviceDriver deviceDriver;
        try {
            deviceDriver = deviceService.getDeviceDriverByToken(token);
        } catch (Exception e) {
            Output<?> output = new Output<>();
            output.setStatus(Output.Status.ERROR);
            output.setMessage("invalid token: " + token);
            OutputSender.send(session, output);
            session.close();
            return;
        }
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
                .addInputHandler(new StopReceiveAppiumLogHandler(session, deviceDriver));
    }

    @OnClose
    public void onClose() {
        log.info("[{}]onClose", session.getId());
        if (token != null) deviceService.unlockDevice(token);
    }

    @OnError
    public void onError(Throwable cause) {
        log.error("[{}]onError", session.getId(), cause);
    }

    @OnMessage
    public void onMessage(String message) {
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
