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
import java.util.function.Consumer;

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

    ScrcpyFrameClient(IDevice iDevice) {
        this.iDevice = iDevice;
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

        while (scrcpyOptions.isSendDummyByte() && frameInputStream.read() != 0) { // 读到0，代表连接成功
            frameInputStream.close();
            frameSocket.close();

            if (System.currentTimeMillis() > timeoutMs) {
                throw new ScrcpyException("read frame timeout in " + readTimeout);
            }

            try {
                Thread.sleep(500);
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
        if (frameSocket != null) {
            try {
                log.info("[{}]shutdown frame socket input", iDevice.getSerialNumber());
                frameSocket.shutdownInput();
            } catch (IOException e) {
                log.warn("[{}]shutdown frame socket input io err", iDevice.getSerialNumber());
            }
        }
        if (frameInputStream != null) {
            try {
                log.info("[{}]close frame input stream", iDevice.getSerialNumber());
                frameInputStream.close();
            } catch (IOException e) {
                log.warn("[{}]close frame input stream io err", iDevice.getSerialNumber(), e);
            }
            frameInputStream = null;
        }
        if (frameSocket != null) {
            try {
                log.info("[{}]close frame socket", iDevice.getSerialNumber());
                frameSocket.close();
            } catch (IOException e) {
                log.warn("[{}]close frame socket io err", iDevice.getSerialNumber(), e);
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
        if (scrcpyOptions.isSendDeviceMeta()) {
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
    }

    /**
     * H264 NALU: H264 NALU（Network Abstraction Layer Unit）是H264视频流的基本单元，由一个start code和紧随其后的视频数据组成
     * start code: 0x000001或0x00000001
     */
    public synchronized void startReadingFrames(Consumer<ByteBuffer> consumer) throws IOException {
        final byte[] buffer = new byte[1024 * 1024];
        final int maxReadLen = 1024; // 每次最多读取
        int bufferOffset = 0;
        int naluOffset;

        while (frameInputStream != null) {
            int readLen = frameInputStream.read(buffer, bufferOffset, maxReadLen);
            if (readLen > 0) {
                bufferOffset += readLen;
                for (int i = 5; i < bufferOffset - 4; i++) {
                    if (buffer[i] == 0x00 && buffer[i + 1] == 0x00
                            && buffer[i + 2] == 0x00 && buffer[i + 3] == 0x01) {
                        naluOffset = i;
                        byte[] nalu = new byte[naluOffset];
                        System.arraycopy(buffer, 0, nalu, 0, naluOffset);
                        consumer.accept(ByteBuffer.wrap(nalu));
                        bufferOffset -= naluOffset;
                        System.arraycopy(buffer, naluOffset, buffer, 0, bufferOffset);
                        i = 5;
                    }
                }
            }
        }
    }

}
