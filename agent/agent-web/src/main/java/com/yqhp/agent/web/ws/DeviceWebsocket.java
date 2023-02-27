package com.yqhp.agent.web.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.service.DeviceService;
import com.yqhp.agent.web.ws.message.InputMessage;
import com.yqhp.agent.web.ws.message.OutputMessage;
import com.yqhp.agent.web.ws.message.OutputMessageSender;
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
            log.warn("[{}]getDeviceDriver err, token={}", session.getId(), token, e);
            OutputMessageSender.send(
                    session,
                    new OutputMessage<>()
                            .setStatus(OutputMessage.Status.ERROR)
                            .setMessage("invalid token: " + token)
            );
            session.close();
            return;
        }
        this.token = token;

        messageHandler = new MessageHandler()
                .addCommandHandler(new StartScrcpyHandler(session, deviceDriver))
                .addCommandHandler(new ScrcpyKeyHandler(session, deviceDriver))
                .addCommandHandler(new ScrcpyTouchHandler(session, deviceDriver))
                .addCommandHandler(new ScrcpyScrollHandler(session, deviceDriver))
                .addCommandHandler(new ReceiveDeviceLogHandler(session, deviceDriver))
                .addCommandHandler(new StopReceiveDeviceLogHandler(session, deviceDriver))
                .addCommandHandler(new ReceiveAppiumLogHandler(session, deviceDriver))
                .addCommandHandler(new StopReceiveAppiumLogHandler(session, deviceDriver));
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
        InputMessage<JsonNode> input;
        try {
            input = messageHandler.readMessage(message);
        } catch (Exception e) {
            log.warn("[{}]invalid message: {}", session.getId(), message, e);
            OutputMessageSender.send(
                    session,
                    new OutputMessage<>()
                            .setStatus(OutputMessage.Status.ERROR)
                            .setMessage(e.getMessage())
            );
            return;
        }

        try {
            messageHandler.handleMessage(input);
        } catch (Exception e) {
            log.warn("[{}]handleMessage err, message={}", session.getId(), message, e);
            OutputMessageSender.send(
                    session,
                    new OutputMessage<>()
                            .setStatus(OutputMessage.Status.ERROR)
                            .setUid(input.getUid())
                            .setCommand(input.getCommand())
                            .setMessage(e.getMessage())
            );
        }
    }

}
