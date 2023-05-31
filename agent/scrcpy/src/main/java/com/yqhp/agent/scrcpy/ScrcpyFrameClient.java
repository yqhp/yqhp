package com.yqhp.agent.scrcpy;

import com.android.ddmlib.IDevice;
import com.yqhp.common.commons.model.Size;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
@Slf4j
public class ScrcpyFrameClient {

    private static final int NALU_START_CODE_LENGTH = 4;
    private static final String REMOTE_SOCKET_NAME = "scrcpy";

    @Getter
    private Size screenSize;

    private final IDevice iDevice;
    private ScrcpyOptions scrcpyOptions;
    private int localPort;

    private SocketChannel socketChannel;

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

        socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", localPort));
        ByteBuffer dummy = ByteBuffer.allocate(1);
        long timeoutMs = System.currentTimeMillis() + readTimeout.toMillis();
        while (scrcpyOptions.isSendDummyByte() && socketChannel.read(dummy) != 1) {
            socketChannel.close();
            if (System.currentTimeMillis() > timeoutMs) {
                throw new ScrcpyException("read dummy byte timeout in " + readTimeout);
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                log.warn("Interrupted", e);
            }
            // reconnect
            socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", localPort));
        }

        log.info("[{}]connect frame socket success", iDevice.getSerialNumber());
    }

    void disconnect() {
        if (socketChannel != null) {
            try {
                log.info("[{}]close frame socket channel", iDevice.getSerialNumber());
                socketChannel.close();
                socketChannel = null;
            } catch (IOException e) {
                log.warn("[{}]close frame socket channel io err", iDevice.getSerialNumber(), e);
            }
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
            ByteBuffer deviceNameBuffer = ByteBuffer.allocate(64);
            socketChannel.read(deviceNameBuffer);
            ByteBuffer widthBuffer = ByteBuffer.allocate(2);
            socketChannel.read(widthBuffer);
            ByteBuffer heightBuffer = ByteBuffer.allocate(2);
            socketChannel.read(heightBuffer);
            widthBuffer.flip();
            int width = widthBuffer.order(ByteOrder.BIG_ENDIAN).getShort();
            heightBuffer.flip();
            int height = heightBuffer.order(ByteOrder.BIG_ENDIAN).getShort();
            log.info("[{}]scrcpy: width={}, height={}", iDevice.getSerialNumber(), width, height);
            screenSize = new Size(width, height);
        }
    }

    public void readFrame(Consumer<ByteBuffer> consumer) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2 * 1024 * 1024);
        while (socketChannel != null && socketChannel.read(buffer) != -1) {
            buffer.flip(); // 切换为读模式

            // 消费buffer中全部nalu
            while (buffer.remaining() > NALU_START_CODE_LENGTH) {
                int naluStart = buffer.position();
                buffer.position(naluStart + NALU_START_CODE_LENGTH);
                int naluEnd = findNALUEndPosition(buffer);
                if (naluEnd == -1) {
                    buffer.position(naluStart);
                    break;
                }
                byte[] naluData = new byte[naluEnd - naluStart];
                buffer.position(naluStart);
                buffer.get(naluData);
                consumer.accept(ByteBuffer.wrap(naluData));
            }

            buffer.compact(); // 切换为写模式，并将剩余的数据移到缓冲区的开头
        }
    }

    private int findNALUEndPosition(ByteBuffer buffer) {
        int startPosition = buffer.position();
        int remaining = buffer.remaining();

        for (int i = 0; i < remaining - 3; i++) {
            if (buffer.get(startPosition + i) == 0x00 &&
                    buffer.get(startPosition + i + 1) == 0x00 &&
                    buffer.get(startPosition + i + 2) == 0x00 &&
                    buffer.get(startPosition + i + 3) == 0x01) {
                return startPosition + i;
            }
        }

        return -1;
    }
}
