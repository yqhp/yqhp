package com.yqhp.agent.web.ws.message;

import com.yqhp.common.commons.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
@Slf4j
public class OutputMessageSender {

    private final Session session;
    private final Command command;

    public OutputMessageSender(Session session, Command command) {
        this.session = session;
        this.command = command;
    }

    public void ok(String uid, String message) {
        send(uid, OutputMessage.Status.OK, message, null);
    }

    public void info(String uid, String message) {
        send(uid, OutputMessage.Status.INFO, message, null);
    }

    public <T> void info(String uid, T data) {
        send(uid, OutputMessage.Status.INFO, null, data);
    }

    public <T> void send(String uid, OutputMessage.Status status, String message, T data) {
        OutputMessage<T> output = new OutputMessage<>();
        output.setUid(uid);
        output.setCommand(command);
        output.setStatus(status);
        output.setMessage(message);
        output.setData(data);
        send(session, output);
    }

    public static <T> void send(Session session, OutputMessage<T> output) {
        try {
            String text = JacksonUtils.writeValueAsString(output);
            session.getBasicRemote().sendText(text);
        } catch (Exception e) {
            log.warn("[{}]send err, output={}", session.getId(), output, e);
        }
    }
}
