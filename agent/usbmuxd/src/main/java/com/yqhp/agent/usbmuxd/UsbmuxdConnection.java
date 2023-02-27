package com.yqhp.agent.usbmuxd;

import com.yqhp.common.commons.system.OS;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author jiangyitao
 */
@Slf4j
class UsbmuxdConnection implements Closeable {

    private static final int WINDOWS_USBMUXD_LOCAL_PORT = 27015;

    private final Socket socket;
    @Getter
    private final InputStream inputStream;
    @Getter
    private final OutputStream outputStream;

    UsbmuxdConnection() {
        try {
            SocketAddress address;
            if (OS.isWindows()) {
                socket = new Socket();
                address = new InetSocketAddress("127.0.0.1", WINDOWS_USBMUXD_LOCAL_PORT);
            } else {
                socket = AFUNIXSocket.newInstance();
                address = AFUNIXSocketAddress.of(new File("/var/run/usbmuxd"));
            }
            socket.connect(address);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            throw new UsbmuxdException(e);
        }
    }

    @Override
    public void close() {
        try {
            outputStream.close();
        } catch (IOException e) {
            log.warn("close outputStream io err", e);
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            log.warn("close inputStream io err", e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            log.warn("close socket io err", e);
        }
    }
}
