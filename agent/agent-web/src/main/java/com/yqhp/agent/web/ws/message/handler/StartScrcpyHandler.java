package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.common.LocalPortProvider;
import com.yqhp.agent.driver.AndroidDeviceDriver;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.scrcpy.Scrcpy;
import com.yqhp.agent.scrcpy.ScrcpyFrameClient;
import com.yqhp.agent.scrcpy.ScrcpyOptions;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;
import com.yqhp.common.web.util.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/**
 * @author jiangyitao
 */
@Slf4j
public class StartScrcpyHandler extends DefaultInputHandler<ScrcpyOptions> {

    private static final ExecutorService START_SCRCPY_THREAD_POOL = Executors.newCachedThreadPool();
    private static final ExecutorService READ_SCRCPY_FRAME_THREAD_POOL = Executors.newCachedThreadPool();

    public StartScrcpyHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.START_SCRCPY;
    }

    @Override
    protected void handle(Input<ScrcpyOptions> input) {
        String uid = input.getUid();

        os.info(uid, "starting scrcpy...");
        Scrcpy scrcpy = ((AndroidDeviceDriver) deviceDriver).getScrcpy();
        scrcpy.start(
                ApplicationContextUtils.getProperty("agent.android.scrcpy-server-path"),
                ApplicationContextUtils.getProperty("agent.android.scrcpy-version"),
                input.getData(),
                LocalPortProvider.getScrcpyAvailablePort(),
                Duration.ofSeconds(30), // start timeout
                START_SCRCPY_THREAD_POOL
        );
        os.info(uid, "starting scrcpy success");

        ScrcpyFrameClient scrcpyFrameClient = scrcpy.getScrcpyFrameClient();
        os.info(uid, scrcpyFrameClient.getScreenSize());

        final BlockingQueue<ByteBuffer> blockingQueue = new SynchronousQueue<>();
        Thread sendFrameThread = new Thread(() -> {
            RemoteEndpoint.Basic remote = session.getBasicRemote();
            try {
                os.ok(uid, "start sending frames...");
                log.info("[{}]start sending frames...", deviceDriver.getDeviceId());
                while (session.isOpen()) {
                    ByteBuffer frame = blockingQueue.take(); // 若take()阻塞在此，sendFrameThread.interrupt()后，take()会抛出InterruptedException
                    remote.sendBinary(frame);
                }
                log.info("[{}]stop sending frames", deviceDriver.getDeviceId());
            } catch (Throwable cause) {
                log.info("[{}]stop sending frames, cause: {}", deviceDriver.getDeviceId(),
                        cause.getMessage() == null ? cause.getClass() : cause.getMessage());
            }
        });
        sendFrameThread.start();

        READ_SCRCPY_FRAME_THREAD_POOL.submit(() -> {
            try {
                log.info("[{}]start reading frames", deviceDriver.getDeviceId());
                while (session.isOpen()) {
                    ByteBuffer frame = scrcpyFrameClient.read();
                    blockingQueue.put(frame);
                }
                log.info("[{}]stop reading frames", deviceDriver.getDeviceId());
            } catch (Throwable cause) {
                log.info("[{}]stop reading frames, cause: {}", deviceDriver.getDeviceId(),
                        cause.getMessage() == null ? cause.getClass() : cause.getMessage());
            } finally {
                sendFrameThread.interrupt();
            }
        });
    }

}
