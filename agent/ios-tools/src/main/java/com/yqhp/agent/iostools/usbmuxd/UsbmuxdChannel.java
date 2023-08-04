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
package com.yqhp.agent.iostools.usbmuxd;

import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.yqhp.common.commons.system.OS;
import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.newsclub.net.unix.AFUNIXSocketChannel;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jiangyitao
 */
@Slf4j
public class UsbmuxdChannel implements Closeable {

    private static final int WINDOWS_USBMUXD_LOCAL_PORT = 27015;
    private static final AtomicInteger TAG = new AtomicInteger(1);

    private final SocketChannel socketChannel;

    public UsbmuxdChannel() {
        try {
            socketChannel = OS.isWindows()
                    ? SocketChannel.open(new InetSocketAddress("127.0.0.1", WINDOWS_USBMUXD_LOCAL_PORT))
                    : AFUNIXSocketChannel.open(AFUNIXSocketAddress.of(new File("/var/run/usbmuxd")));
        } catch (IOException e) {
            throw new UsbmuxdException(e);
        }
    }

    @Override
    public void close() {
        if (socketChannel != null) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                log.warn("close socket channel io err", e);
            }
        }
    }

    public NSObject writeAndRead(NSObject req) {
        write(req);
        return read();
    }

    public void write(NSObject req) {
        if (req == null) return;
        byte[] payload = req.toXMLPropertyList().getBytes(StandardCharsets.UTF_8);
        // header + payload
        int packetLen = 16 + payload.length;
        ByteBuffer packet = ByteBuffer.allocate(packetLen);
        packet.order(ByteOrder.LITTLE_ENDIAN);
        // header
        packet.putInt(packetLen);
        packet.putInt(1);
        packet.putInt(8);
        packet.putInt(TAG.getAndIncrement());
        // payload
        packet.put(payload);
        packet.flip();
        try {
            socketChannel.write(packet);
        } catch (IOException e) {
            throw new UsbmuxdException(e);
        }
    }

    private final ByteBuffer respHeaderBuffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);

    public NSObject read() {
        try {
            respHeaderBuffer.clear();
            socketChannel.read(respHeaderBuffer);
            // header前4个字节标识响应长度，-header=payload
            int payloadLen = respHeaderBuffer.getInt(0) - respHeaderBuffer.capacity();
            ByteBuffer payload = ByteBuffer.allocate(payloadLen);
            socketChannel.read(payload);
            return PropertyListParser.parse(payload.array());
        } catch (Exception e) {
            throw new UsbmuxdException(e);
        }
    }
}
