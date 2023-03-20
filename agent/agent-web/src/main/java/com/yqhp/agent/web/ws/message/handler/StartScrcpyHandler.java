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
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jiangyitao
 */
@Slf4j
public class StartScrcpyHandler extends DefaultInputHandler<ScrcpyOptions> {

    private static final ExecutorService START_SCRCPY_THREAD_POOL = Executors.newCachedThreadPool();
    private static final ExecutorService SEND_SCRCPY_FRAME_THREAD_POOL = Executors.newCachedThreadPool();

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

        SEND_SCRCPY_FRAME_THREAD_POOL.submit(() -> {
            RemoteEndpoint.Basic remote = session.getBasicRemote();
            ScrcpyFrameClient scrcpyFrameClient = scrcpy.getScrcpyFrameClient();
            os.info(uid, scrcpyFrameClient.getScreenSize());
            try {
                os.ok(uid, "start sending screen frames...");
                log.info("[{}]start sending screen frames...", deviceDriver.getDeviceId());
                for (; ; ) {
                    remote.sendBinary(scrcpyFrameClient.readFrame());
                }
            } catch (Throwable cause) {
                log.info("[{}]stop sending screen frames, cause: {}", deviceDriver.getDeviceId(), cause.getMessage());
            }
        });
    }

}
