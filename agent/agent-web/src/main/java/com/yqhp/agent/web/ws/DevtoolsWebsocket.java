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

import com.android.ddmlib.IDevice;
import com.yqhp.agent.devicediscovery.android.AndroidDevice;
import com.yqhp.agent.driver.DeviceDriver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.SocketUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

/**
 * @author jiangyitao
 */
@Slf4j
@Controller
@ServerEndpoint(value = "/device/devtools/token/{token}/socket/{socket}/page/{page}")
public class DevtoolsWebsocket {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private int localPort;
    private IDevice iDevice;
    private WebSocket devtoolsWS;

    @OnOpen
    public void onOpen(@PathParam("token") String token,
                       @PathParam("socket") String socket,
                       @PathParam("page") String page,
                       Session session) throws Exception {
        log.info("[devtools][{}]onOpen, token:{}, socket:{}, page:{}", session.getId(), token, socket, page);
        DeviceDriver deviceDriver = DeviceWebsocket.deviceService.getDeviceDriverByToken(token);

        localPort = SocketUtils.findAvailableTcpPort();
        iDevice = ((AndroidDevice) (deviceDriver.getDevice())).getIDevice();
        log.info("[devtools][{}]createForward {} -> {}", session.getId(), localPort, socket);
        iDevice.createForward(localPort, socket, IDevice.DeviceUnixSocketNamespace.ABSTRACT);

        WebSocket.Listener devtoolsWSListener = new WebSocket.Listener() {

            @Override
            public void onOpen(WebSocket webSocket) {
                log.info("[devtools][{}][proxy]onOpen", session.getId());
                WebSocket.Listener.super.onOpen(webSocket);
            }

            @SneakyThrows
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                session.getBasicRemote().sendText(data.toString(), last);
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @SneakyThrows
            @Override
            public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                session.getBasicRemote().sendBinary(data, last);
                return WebSocket.Listener.super.onBinary(webSocket, data, last);
            }

            @SneakyThrows
            @Override
            public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
                session.getBasicRemote().sendPing(message);
                return WebSocket.Listener.super.onPing(webSocket, message);
            }

            @SneakyThrows
            @Override
            public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
                session.getBasicRemote().sendPong(message);
                return WebSocket.Listener.super.onPong(webSocket, message);
            }

            @SneakyThrows
            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                log.info("[devtools][{}][proxy]onClose, statusCode={}, reason={}", session.getId(), statusCode, reason);
                session.close();
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }
        };
        devtoolsWS = HTTP_CLIENT.newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:" + localPort + "/devtools/page/" + page), devtoolsWSListener)
                .join();
    }

    @OnError
    public void onError(Throwable cause, Session session) {
        log.warn("[devtools][{}]onError, cause:{}", session.getId(), cause.getMessage());
    }

    @OnClose
    public void onClose(Session session) throws Exception {
        log.info("[devtools][{}]onClose", session.getId());
        try {
            if (isDevtoolsWSOutputAvailable()) {
                devtoolsWS.sendClose(WebSocket.NORMAL_CLOSURE, "");
            }
        } finally {
            if (iDevice != null && localPort > 0) {
                log.info("[devtools][{}]removeForward {}", session.getId(), localPort);
                iDevice.removeForward(localPort);
            }
        }
    }

    @OnMessage
    public void onMessage(String message) {
        if (isDevtoolsWSOutputAvailable()) {
            devtoolsWS.sendText(message, true);
        }
    }

    private boolean isDevtoolsWSOutputAvailable() {
        return devtoolsWS != null && !devtoolsWS.isOutputClosed();
    }
}
