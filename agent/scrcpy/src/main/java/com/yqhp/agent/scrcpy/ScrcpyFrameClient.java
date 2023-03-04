package com.yqhp.agent.scrcpy;

import com.android.ddmlib.IDevice;
import com.yqhp.common.commons.model.Size;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * @author jiangyitao
 */
@Slf4j
public class ScrcpyFrameClient {

    private static final String REMOTE_SOCKET_NAME = "scrcpy";

    @Getter
    private Size screenSize;

    private final IDevice iDevice;
    private ScrcpyOptions scrcpyOptions;
    private int localPort;

    private Socket frameSocket;
    private InputStream frameInputStream;
    private byte[] frameBuffer;

    ScrcpyFrameClient(IDevice iDevice) {
        this.iDevice = iDevice;
        frameBuffer = new byte[1024 * 1024];
    }

    void connect(int localPort, Duration readTimeout, ScrcpyOptions scrcpyOptions) throws IOException {
        this.scrcpyOptions = scrcpyOptions;

        if (scrcpyOptions.isTunnelForward()) {
            try {
                log.info("[{}]adb forward {} -> remote {}", iDevice.getSerialNumber(), localPort, REMOTE_SOCKET_NAME);
                iDevice.createForward(localPort, REMOTE_SOCKET_NAME, IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            } catch (Exception e) {
                throw new ScrcpyException(e);
            }
        } else {
            // todo iDevice.createReverse
        }
        this.localPort = localPort;

        frameSocket = new Socket("127.0.0.1", localPort);
        frameInputStream = frameSocket.getInputStream();

        long timeoutMs = System.currentTimeMillis() + readTimeout.toMillis();

        while (frameInputStream.read() != 0) { // scrcpy协议: 读到0，代表连接成功
            frameInputStream.close();
            frameSocket.close();

            if (System.currentTimeMillis() > timeoutMs) {
                throw new ScrcpyException("read frame timeout in " + readTimeout);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.warn("Interrupted", e);
            }

            // reconnect
            frameSocket = new Socket("127.0.0.1", localPort);
            frameInputStream = frameSocket.getInputStream();
        }

        log.info("[{}]connect frame socket success", iDevice.getSerialNumber());
    }

    void disconnect() {
        if (frameInputStream != null) {
            try {
                log.info("[{}]close frame input stream", iDevice.getSerialNumber());
                frameInputStream.close();
            } catch (IOException e) {
                log.warn("close frame input stream io err", e);
            }
            frameInputStream = null;
        }

        if (frameSocket != null) {
            try {
                log.info("[{}]close frame socket", iDevice.getSerialNumber());
                frameSocket.close();
            } catch (IOException e) {
                log.warn("close frame socket io err", e);
            }
            frameSocket = null;
        }

        if (localPort > 0) {
            if (scrcpyOptions.isTunnelForward()) {
                try {
                    log.info("[{}]adb remove forward {} -> remote {}", iDevice.getSerialNumber(), localPort, REMOTE_SOCKET_NAME);
                    iDevice.removeForward(localPort);
                } catch (Exception e) {
                    log.warn("[{}]adb remove forward err", iDevice.getSerialNumber(), e);
                }
            } else {
                // todo iDevice.removeReverse
            }
            localPort = 0;
        }
    }

    void readDeviceMeta() throws IOException {
        // deviceName 64字节，暂时用不到，忽略
        for (int i = 0; i < 64; i++) {
            frameInputStream.read();
        }

        // width height 2字节
        int width = frameInputStream.read() << 8 | frameInputStream.read();
        int height = frameInputStream.read() << 8 | frameInputStream.read();
        log.info("[{}]scrcpy: width={}, height={}", iDevice.getSerialNumber(), width, height);
        screenSize = new Size(width, height);
    }

    public ByteBuffer readFrame() throws IOException {
        int frameSize;
        if (scrcpyOptions.isSendFrameMeta()) {
            // presentationTimeUs 8字节，暂时用不到，忽略
            for (int i = 0; i < 8; i++) {
                frameInputStream.read();
            }

            // packetSize 4字节
            frameSize = frameInputStream.read() << 24 | frameInputStream.read() << 16
                    | frameInputStream.read() << 8 | frameInputStream.read();

            if (frameSize > frameBuffer.length) {
                frameBuffer = new byte[frameSize]; // 扩容
            }

            for (int i = 0; i < frameSize; i++) {
                frameBuffer[i] = (byte) frameInputStream.read();
            }
        } else {
            frameSize = frameInputStream.read(frameBuffer);
        }
        return ByteBuffer.wrap(frameBuffer, 0, frameSize);
    }

}
