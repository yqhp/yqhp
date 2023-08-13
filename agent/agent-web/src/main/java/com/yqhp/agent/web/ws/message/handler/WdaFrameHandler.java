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

import com.yqhp.agent.driver.IOSDeviceDriver;
import com.yqhp.agent.iostools.WdaUtils;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;
import com.yqhp.agent.web.ws.message.OutputSender;
import com.yqhp.common.commons.model.Size;
import com.yqhp.common.commons.util.MjpegInputStream;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/**
 * @author jiangyitao
 */
@Slf4j
public class WdaFrameHandler extends InputHandler {

    private static final ExecutorService READ_FRAME_THREAD_POOL = Executors.newCachedThreadPool();

    private final Session session;
    private final OutputSender os;
    private final IOSDeviceDriver driver;

    public WdaFrameHandler(Session session, IOSDeviceDriver driver) {
        this.session = session;
        os = new OutputSender(session, command());
        this.driver = driver;
    }

    @Override
    protected Command command() {
        return Command.WDA_FRAME;
    }

    @Override
    protected void handle(Input input) throws Exception {
        String uid = input.getUid();

        os.info(uid, "run wda...");
        driver.runWdaIfNeeded();
        os.info(uid, "run wda successfully");

        log.info("[ios][{}]create wdaSession", driver.getDeviceId());
        driver.createWdaSession();
        log.info("[ios][{}]wdaSession created", driver.getDeviceId());

        // 获取逻辑分辨率传给前端，后续坐标相关操作，基于该分辨率
        Size size = WdaUtils.getLogicalScreenSize(driver.getWdaUrl(), driver.getWdaSessionId());
        log.info("[ios][{}]logical screen size: {}", driver.getDeviceId(), size);
        os.info(uid, size);

        String wdaMjpegUrl = driver.getWdaMjpegUrl();
        HttpURLConnection conn = (HttpURLConnection) new URL(wdaMjpegUrl).openConnection();
        conn.setConnectTimeout(1000); // ms
        conn.setReadTimeout(30_000); // ms
        int connectCount = 0; // 记录连接次数
        for (; ; ) {
            try {
                connectCount++;
                log.info("[ios][{}]connect to wdaMjpegUrl {} ..{}", driver.getDeviceId(), wdaMjpegUrl, connectCount);
                conn.connect();
                log.info("[ios][{}]wdaMjpegUrl connected", driver.getDeviceId());
                os.info(uid, "wdaMjpegUrl connected");
                break;
            } catch (Exception e) {
                log.info("[ios][{}]connect to wdaMjpegUrl failed, reason={}", driver.getDeviceId(), e.getMessage());
                if (connectCount == 10) {
                    throw new IllegalStateException("cannot connect to wdaMjpegUrl " + wdaMjpegUrl);
                }
                Thread.sleep(300);
            }
        }

        final BlockingQueue<byte[]> blockingQueue = new SynchronousQueue<>();
        Thread sendFrameThread = new Thread(() -> {
            RemoteEndpoint.Basic remote = session.getBasicRemote();
            try {
                os.ok(uid, "start sending frames...");
                log.info("[ios][{}]start sending frames...", driver.getDeviceId());
                while (session.isOpen()) {
                    byte[] frame = blockingQueue.take();// 若take()阻塞在此，sendFrameThread.interrupt()后，take()会抛出InterruptedException
                    remote.sendBinary(ByteBuffer.wrap(frame));
                }
                log.info("[ios][{}]stop sending frames", driver.getDeviceId());
            } catch (Throwable cause) {
                log.info("[ios][{}]stop sending frames, cause: {}", driver.getDeviceId(),
                        cause.getMessage() == null ? cause.getClass() : cause.getMessage());
            }
        });
        sendFrameThread.start();

        READ_FRAME_THREAD_POOL.submit(() -> {
            try {
                log.info("[ios][{}]start reading frames", driver.getDeviceId());
                try (InputStream is = conn.getInputStream();
                     MjpegInputStream mis = new MjpegInputStream(is)) {
                    while (session.isOpen()) {
                        byte[] frame = mis.readFrame();
                        blockingQueue.put(frame);
                    }
                }
                log.info("[ios][{}]stop reading frames", driver.getDeviceId());
            } catch (Throwable cause) {
                log.info("[ios][{}]stop reading frames, cause: {}", driver.getDeviceId(),
                        cause.getMessage() == null ? cause.getClass() : cause.getMessage());
            } finally {
                conn.disconnect();
                sendFrameThread.interrupt();
            }
        });
    }

}
