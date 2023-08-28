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
package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.common.LocalPortProvider;
import com.yqhp.agent.driver.AndroidDeviceDriver;
import com.yqhp.agent.scrcpy.Scrcpy;
import com.yqhp.agent.scrcpy.ScrcpyFrameClient;
import com.yqhp.agent.scrcpy.ScrcpyOptions;
import com.yqhp.agent.web.config.Properties;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;
import com.yqhp.agent.web.ws.message.OutputSender;
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
public class ScrcpyFrameHandler extends InputHandler<ScrcpyOptions> {

    private static final ExecutorService START_SCRCPY_THREAD_POOL = Executors.newCachedThreadPool();
    private static final ExecutorService READ_SCRCPY_FRAME_THREAD_POOL = Executors.newCachedThreadPool();

    private final Session session;
    private final OutputSender os;
    private final AndroidDeviceDriver driver;

    public ScrcpyFrameHandler(Session session, AndroidDeviceDriver driver) {
        this.session = session;
        os = new OutputSender(session, command());
        this.driver = driver;
    }

    @Override
    protected Command command() {
        return Command.SCRCPY_FRAME;
    }

    @Override
    protected void handle(Input<ScrcpyOptions> input) {
        String uid = input.getUid();

        os.info(uid, "Start scrcpy...");
        Scrcpy scrcpy = driver.getScrcpy();
        scrcpy.start(
                Properties.getScrcpyServerPath(),
                Properties.getScrcpyVersion(),
                input.getData(),
                LocalPortProvider.getScrcpyAvailablePort(),
                Duration.ofSeconds(30), // start timeout
                START_SCRCPY_THREAD_POOL
        );
        os.info(uid, "Scrcpy started");

        ScrcpyFrameClient scrcpyFrameClient = scrcpy.getScrcpyFrameClient();
        os.info(uid, scrcpyFrameClient.getScreenSize());

        final BlockingQueue<ByteBuffer> blockingQueue = new SynchronousQueue<>();
        Thread sendFrameThread = new Thread(() -> {
            RemoteEndpoint.Basic remote = session.getBasicRemote();
            try {
                os.ok(uid, "Start sending frames...");
                log.info("[{}]Start sending frames...", driver.getDeviceId());
                while (session.isOpen()) {
                    ByteBuffer frame = blockingQueue.take(); // 若take()阻塞在此，sendFrameThread.interrupt()后，take()会抛出InterruptedException
                    remote.sendBinary(frame);
                }
                log.info("[{}]Stop sending frames", driver.getDeviceId());
            } catch (Throwable cause) {
                log.info("[{}]Stop sending frames, cause: {}", driver.getDeviceId(),
                        cause.getMessage() == null ? cause.getClass() : cause.getMessage());
            }
        });
        sendFrameThread.start();

        READ_SCRCPY_FRAME_THREAD_POOL.submit(() -> {
            try {
                log.info("[{}]Start reading frames", driver.getDeviceId());
                scrcpyFrameClient.readFrame(frame -> {
                    try {
                        blockingQueue.put(frame);
                    } catch (InterruptedException e) {
                        log.warn("[{}]Put frame interrupted", driver.getDeviceId());
                    }
                });
                log.info("[{}]Stop reading frames", driver.getDeviceId());
            } catch (Throwable cause) {
                log.info("[{}]Stop reading frames, cause: {}", driver.getDeviceId(),
                        cause.getMessage() == null ? cause.getClass() : cause.getMessage());
            } finally {
                sendFrameThread.interrupt();
            }
        });
    }

}
