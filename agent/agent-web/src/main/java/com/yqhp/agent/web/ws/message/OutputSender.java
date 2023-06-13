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
        if (!session.isOpen()) {
            return;
        }
        try {
            String text = JacksonUtils.writeValueAsString(output);
            session.getBasicRemote().sendText(text);
        } catch (Exception e) {
            log.warn("[{}]send err, output={}", session.getId(), output, e);
        }
    }
}
