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
package com.yqhp.common.web.util;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author jiangyitao
 */
public class WebsocketUtils {

    /**
     * session发送数据不是线程安全的
     * 多线程发送会出现IllegalStateException: The remote endpoint was in state [TEXT_FULL_WRITING / BINARY_FULL_WRITING] which is an invalid state for called method
     * 通过synchronized (session) 保证线程安全
     */
    public static void sendText(Session session, String text) throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(text);
                }
            }
        }
    }

    public static void sendText(Session session, String text, boolean last) throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(text, last);
                }
            }
        }
    }

    public static void sendBinary(Session session, ByteBuffer data) throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendBinary(data);
                }
            }
        }
    }

    public static void sendBinary(Session session, ByteBuffer data, boolean last) throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendBinary(data, last);
                }
            }
        }
    }

    public static void sendPing(Session session, ByteBuffer data) throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendPing(data);
                }
            }
        }
    }

    public static void sendPong(Session session, ByteBuffer data) throws IOException {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendPong(data);
                }
            }
        }
    }
}
