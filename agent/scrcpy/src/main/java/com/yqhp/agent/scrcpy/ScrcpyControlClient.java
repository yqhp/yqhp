package com.yqhp.agent.scrcpy;

import com.android.ddmlib.IDevice;
import com.yqhp.agent.scrcpy.message.KeyEvent;
import com.yqhp.agent.scrcpy.message.ScrollEvent;
import com.yqhp.agent.scrcpy.message.TouchEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
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

    private Socket controlSocket;
    private OutputStream controlOutputStream;

    private final IDevice iDevice;

    ScrcpyControlClient(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    void connect(int localPort) throws IOException {
        controlSocket = new Socket("127.0.0.1", localPort);
        controlOutputStream = controlSocket.getOutputStream();
        log.info("[{}]connect control socket success", iDevice.getSerialNumber());
    }

    void disconnect() {
        if (controlOutputStream != null) {
            try {
                log.info("[{}]close control output stream", iDevice.getSerialNumber());
                controlOutputStream.close();
            } catch (IOException e) {
                log.warn("close control output stream io err", e);
            }
            controlOutputStream = null;
        }

        if (controlSocket != null) {
            try {
                log.info("[{}]close control socket", iDevice.getSerialNumber());
                controlSocket.close();
            } catch (IOException e) {
                log.warn("close control socket io err", e);
            }
            controlSocket = null;
        }
    }

    public void sendTouchEvent(TouchEvent event) throws IOException {
        touchEventBuffer.rewind();

        touchEventBuffer.put(INJECT_TOUCH_EVENT);
        touchEventBuffer.put(event.getAction());
        touchEventBuffer.putLong(event.getPointerId());
        touchEventBuffer.putInt(event.getX());
        touchEventBuffer.putInt(event.getY());
        touchEventBuffer.putShort(event.getWidth());
        touchEventBuffer.putShort(event.getHeight());
        touchEventBuffer.putShort(event.getPressure());
        touchEventBuffer.putInt(event.getButtons());

        controlOutputStream.write(touchEventBuffer.array());
    }

    public void sendKeyEvent(KeyEvent event) throws IOException {
        keyEventBuffer.rewind();

        keyEventBuffer.put(INJECT_KEYCODE);
        keyEventBuffer.put(event.getAction());
        keyEventBuffer.putInt(event.getCode());
        keyEventBuffer.putInt(event.getRepeat());
        keyEventBuffer.putInt(event.getMetaState());

        controlOutputStream.write(keyEventBuffer.array());
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
        controlOutputStream.write(textEventBuffer.array());
    }

    public void sendScrollEvent(ScrollEvent event) throws IOException {
        scrollEventBuffer.rewind();

        scrollEventBuffer.put(INJECT_SCROLL_EVENT);
        scrollEventBuffer.putInt(event.getX());
        scrollEventBuffer.putInt(event.getY());
        scrollEventBuffer.putShort(event.getWidth());
        scrollEventBuffer.putShort(event.getHeight());
        scrollEventBuffer.putShort(event.getScrollX());
        scrollEventBuffer.putShort(event.getScrollY());
        scrollEventBuffer.putInt(event.getButtons());

        controlOutputStream.write(scrollEventBuffer.array());
    }

}
