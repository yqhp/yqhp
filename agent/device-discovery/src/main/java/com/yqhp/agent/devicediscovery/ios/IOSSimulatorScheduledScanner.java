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
package com.yqhp.agent.devicediscovery.ios;

import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.agent.iostools.IOSUtils;
import com.yqhp.agent.iostools.Simulator;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangyitao
 */
@Slf4j
class IOSSimulatorScheduledScanner {

    private static final ScheduledExecutorService SCHEDULED_SERVICE = Executors.newSingleThreadScheduledExecutor();

    private static Set<Simulator> lastSimulators = new HashSet<>();
    private static Set<Simulator> currSimulators;

    private static volatile boolean running = false;

    static synchronized void start(Duration scanPeriod, DeviceChangeListener listener) {
        if (running)
            throw new IllegalStateException("Scanner is running");

        if (scanPeriod == null) scanPeriod = Duration.ofSeconds(30);
        long scanPeriodMs = scanPeriod.toMillis();

        SCHEDULED_SERVICE.scheduleAtFixedRate(() -> {
            currSimulators = IOSUtils.listBootedSimulator();

            currSimulators.stream()
                    .filter(curr -> !lastSimulators.contains(curr))
                    .forEach(curr -> listener.online(new IOSSimulator(curr)));
            lastSimulators.stream()
                    .filter(last -> !currSimulators.contains(last))
                    .forEach(last -> listener.offline(new IOSSimulator(last)));

            lastSimulators = currSimulators;
        }, 0, scanPeriodMs, TimeUnit.MILLISECONDS);

        running = true;
    }

    static synchronized void stop() {
        if (running) {
            SCHEDULED_SERVICE.shutdown();
            running = false;
        }
    }
}
