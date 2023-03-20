package com.yqhp.agent.web.ws.message;

import com.yqhp.common.commons.util.JacksonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
@Slf4j
public class OutputSender {

    private final Session session;
    private final Command command;

    public OutputSender(Session session, Command command) {
        this.session = session;
        this.command = command;
    }

    public void ok(String uid, String message) {
        send(uid, Output.Status.OK, message, null);
    }

    public void info(String uid, String message) {
        send(uid, Output.Status.INFO, message, null);
    }

    public <T> void info(String uid, T data) {
        send(uid, Output.Status.INFO, null, data);
    }

    public <T> void send(String uid, Output.Status status, String message, T data) {
        Output<T> output = new Output<>();
        output.setUid(uid);
        output.setCommand(command);
        output.setStatus(status);
        output.setMessage(message);
        output.setData(data);
        send(session, output);
    }

    public static <T> void send(Session session, Output<T> output) {
        try {
            String text = JacksonUtils.writeValueAsString(output);
            session.getBasicRemote().sendText(text);
        } catch (Exception e) {
            log.warn("[{}]send err, output={}", session.getId(), output, e);
        }
    }
}
