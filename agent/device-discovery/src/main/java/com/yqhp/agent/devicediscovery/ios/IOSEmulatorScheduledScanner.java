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
class IOSEmulatorScheduledScanner {

    private static final IOSEmulatorScheduledScanner SINGLE_INSTANCE = new IOSEmulatorScheduledScanner();

    private final ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();

    private Set<Simulator> lastSimulators = new HashSet<>();
    private Set<Simulator> currSimulators;

    private volatile boolean running = false;

    private IOSEmulatorScheduledScanner() {
    }

    static IOSEmulatorScheduledScanner getInstance() {
        return SINGLE_INSTANCE;
    }

    synchronized void start(Duration scanPeriod, DeviceChangeListener listener) {
        if (running)
            throw new IllegalStateException("Scanner is running");

        if (scanPeriod == null) scanPeriod = Duration.ofSeconds(30);
        long scanPeriodMs = scanPeriod.toMillis();

        scheduledService.scheduleAtFixedRate(() -> {
            currSimulators = IOSUtils.listBootedSimulator();

            currSimulators.stream()
                    .filter(currSimulator -> !lastSimulators.contains(currSimulator))
                    .forEach(currSimulator -> listener.online(new IOSEmulator(currSimulator.getModel(), currSimulator.getId())));
            lastSimulators.stream()
                    .filter(lastSimulator -> !currSimulators.contains(lastSimulator))
                    .forEach(lastSimulator -> listener.offline(new IOSEmulator(lastSimulator.getModel(), lastSimulator.getId())));

            lastSimulators = currSimulators;
        }, 0, scanPeriodMs, TimeUnit.MILLISECONDS);

        running = true;
    }

    synchronized void stop() {
        if (running) {
            scheduledService.shutdown();
            running = false;
        }
    }
}
