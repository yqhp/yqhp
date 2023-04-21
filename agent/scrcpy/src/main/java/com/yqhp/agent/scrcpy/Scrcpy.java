package com.yqhp.agent.scrcpy;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangyitao
 */
@Slf4j
@Getter
public class Scrcpy {

    private static final String REMOTE_SCRCPY_PATH = "/data/local/tmp/scrcpy-server.jar";

    private final IDevice iDevice;
    private final ScrcpyFrameClient scrcpyFrameClient;
    private final ScrcpyControlClient scrcpyControlClient;

    private volatile boolean running = false;

    public Scrcpy(IDevice iDevice) {
        this.iDevice = iDevice;
        scrcpyFrameClient = new ScrcpyFrameClient(iDevice);
        scrcpyControlClient = new ScrcpyControlClient(iDevice);
    }

    public synchronized void start(String scrcpyServerFilePath, String scrcpyVersion, ScrcpyOptions options,
                                   int localPort, Duration timeout, Executor executor) {
        if (running)
            throw new ScrcpyException("scrcpy is running");

        pushScrcpyServerFileToDevice(scrcpyServerFilePath);
        running = true; // 为了防止isCancelled返回true导致scrcpy退出，先把running变为true
        final ScrcpyOptions scrcpyOptions = options == null ? new ScrcpyOptions() : options;
        Runnable startScrcpy = () -> {
            String startScrcpyCmd = new StringJoiner(" ")
                    .add("CLASSPATH=" + REMOTE_SCRCPY_PATH)
                    .add("app_process")
                    .add("/")
                    .add("com.genymobile.scrcpy.Server")
                    .add(scrcpyVersion)
                    .add(scrcpyOptions.asString())
                    .toString();
            try {
                log.info("[{}]start scrcpy: {}", iDevice.getSerialNumber(), startScrcpyCmd);
                iDevice.executeShellCommand(startScrcpyCmd, new MultiLineReceiver() {
                    @Override
                    public void processNewLines(String[] lines) {
                        for (String line : lines) {
                            log.info("[{}][scrcpy]{}", iDevice.getSerialNumber(), line);
                        }
                    }

                    /**
                     * @return true: scrcpy将停止运行
                     */
                    @Override
                    public boolean isCancelled() {
                        return !running;
                    }
                }, 0, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("[{}]start scrcpy err", iDevice.getSerialNumber(), e);
            }
            log.info("[{}]scrcpy has exited", iDevice.getSerialNumber());
        };

        // 启动scrcpy
        if (executor != null) {
            executor.execute(startScrcpy);
        } else {
            new Thread(startScrcpy).start();
        }

        // 连接scrcpy
        try {
            scrcpyFrameClient.connect(localPort, timeout, scrcpyOptions);
            scrcpyControlClient.connect(localPort);
            scrcpyFrameClient.readDeviceMeta();
        } catch (Exception e) {
            if (e instanceof ScrcpyException) {
                throw (ScrcpyException) e;
            }
            throw new ScrcpyException(e);
        }

        log.info("[{}]start scrcpy success", iDevice.getSerialNumber());
    }

    public synchronized void stop() {
        scrcpyControlClient.disconnect();
        scrcpyFrameClient.disconnect();
        running = false;
    }

    private void pushScrcpyServerFileToDevice(String scrcpyServerFilePath) {
        try {
            log.info("[{}]push {} to device:{}", iDevice.getSerialNumber(), scrcpyServerFilePath, REMOTE_SCRCPY_PATH);
            iDevice.pushFile(scrcpyServerFilePath, REMOTE_SCRCPY_PATH);
        } catch (Exception e) {
            throw new ScrcpyException(e);
        }
    }
}
