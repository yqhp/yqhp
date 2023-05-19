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

    private ByteBuffer readBuffer;
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
        readBuffer = null;
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

    public ByteBuffer read() throws IOException {
        if (readBuffer == null) {
            // 为了降低闲置时的内存消耗，只有read的时候才去创建buffer，disconnect时将readBuffer置null，使readBuffer可以被回收
            readBuffer = ByteBuffer.allocate(2 * 1024 * 1024);
        }
        readBuffer.clear();
        socketChannel.read(readBuffer);
        byte[] arr = readBuffer.array();
        boolean isNalu = (arr[0] == 0 && arr[1] == 0 && arr[2] == 0 && arr[3] == 1)
                || (arr[0] == 0 && arr[1] == 0 && arr[2] == 1);
        return isNalu
                ? ByteBuffer.wrap(readBuffer.array(), 0, readBuffer.position())
                : null;
    }
}
