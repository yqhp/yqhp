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
package com.yqhp.agent.scrcpy;

import com.android.ddmlib.IDevice;
import com.yqhp.agent.scrcpy.message.KeyEvent;
import com.yqhp.agent.scrcpy.message.ScrollEvent;
import com.yqhp.agent.scrcpy.message.TouchEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author jiangyitao
 */
@Slf4j
public class ScrcpyControlClient {

    private static final byte INJECT_KEYCODE = 0;
    private static final byte INJECT_TEXT = 1;
    private static final byte INJECT_TOUCH_EVENT = 2;
    private static final byte INJECT_SCROLL_EVENT = 3;

    private final ByteBuffer touchEventBuffer = ByteBuffer.allocate(28);
    private final ByteBuffer keyEventBuffer = ByteBuffer.allocate(14);
    private final ByteBuffer scrollEventBuffer = ByteBuffer.allocate(21);

    private SocketChannel socketChannel;

    private final IDevice iDevice;

    ScrcpyControlClient(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    void connect(int localPort) throws IOException {
        socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", localPort));
        log.info("[{}]controlSocketChannel connected", iDevice.getSerialNumber());
    }

    void disconnect() {
        if (socketChannel != null) {
            try {
                log.info("[{}]close controlSocketChannel", iDevice.getSerialNumber());
                socketChannel.close();
                socketChannel = null;
            } catch (IOException e) {
                log.warn("[{}]close controlSocketChannel io err", iDevice.getSerialNumber(), e);
            }
        }
    }

    public void sendTouchEvent(TouchEvent event) throws IOException {
        touchEventBuffer.clear();
        touchEventBuffer.put(INJECT_TOUCH_EVENT);
        touchEventBuffer.put(event.getAction());
        touchEventBuffer.putLong(event.getPointerId());
        touchEventBuffer.putInt(event.getX());
        touchEventBuffer.putInt(event.getY());
        touchEventBuffer.putShort(event.getWidth());
        touchEventBuffer.putShort(event.getHeight());
        touchEventBuffer.putShort(event.getPressure());
        touchEventBuffer.putInt(event.getButtons());
        touchEventBuffer.flip();
        socketChannel.write(touchEventBuffer);
    }

    public void sendKeyEvent(KeyEvent event) throws IOException {
        keyEventBuffer.clear();
        keyEventBuffer.put(INJECT_KEYCODE);
        keyEventBuffer.put(event.getAction());
        keyEventBuffer.putInt(event.getCode());
        keyEventBuffer.putInt(event.getRepeat());
        keyEventBuffer.putInt(event.getMetaState());
        keyEventBuffer.flip();
        socketChannel.write(keyEventBuffer);
    }

    public void sendTextEvent(String text) throws IOException {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ByteBuffer textEventBuffer = ByteBuffer.allocate(5 + bytes.length);
        textEventBuffer.put(INJECT_TEXT);
        textEventBuffer.putInt(bytes.length);
        textEventBuffer.put(bytes);
        textEventBuffer.flip();
        socketChannel.write(textEventBuffer);
    }

    public void sendScrollEvent(ScrollEvent event) throws IOException {
        scrollEventBuffer.clear();
        scrollEventBuffer.put(INJECT_SCROLL_EVENT);
        scrollEventBuffer.putInt(event.getX());
        scrollEventBuffer.putInt(event.getY());
        scrollEventBuffer.putShort(event.getWidth());
        scrollEventBuffer.putShort(event.getHeight());
        scrollEventBuffer.putShort(event.getScrollX());
        scrollEventBuffer.putShort(event.getScrollY());
        scrollEventBuffer.putInt(event.getButtons());
        scrollEventBuffer.flip();
        socketChannel.write(scrollEventBuffer);
    }

}
