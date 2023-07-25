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
package com.yqhp.agent.web.ws;

import com.yqhp.agent.web.ws.message.Output;
import com.yqhp.agent.web.ws.message.OutputSender;
import com.yqhp.agent.web.ws.message.handler.MessageHandler;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;

/**
 * @author jiangyitao
 */
@Slf4j
public class BaseWebsocket {

    protected MessageHandler messageHandler = new MessageHandler();

    /**
     * 注意，子类Override需要带上@OnError注解生效
     */
    @OnError
    public void onError(Throwable cause, Session session) {
        log.warn("[{}]onError, cause:{}", session.getId(), cause.getMessage());
        Output<?> output = new Output<>();
        output.setStatus(Output.Status.ERROR);
        output.setMessage(cause.getMessage());
        OutputSender.send(session, output);
    }

    /**
     * 注意，子类Override需要带上@OnClose才能生效
     */
    @OnClose
    public void onClose(Session session) {
        log.info("[{}]onClose", session.getId());
        onClosed();
    }

    protected void onClosed() {

    }

    /**
     * 注意，子类Override需要带上@OnMessage才能生效
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        messageHandler.handle(message, ((input, cause) -> {
            log.warn("[{}]onMessage err, message={}", session.getId(), message, cause);
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
