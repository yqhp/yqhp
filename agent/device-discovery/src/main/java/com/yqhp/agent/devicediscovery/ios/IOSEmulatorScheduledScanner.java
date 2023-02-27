package com.yqhp.agent.devicediscovery.ios;

import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.common.commons.system.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jiangyitao
 */
@Slf4j
class IOSEmulatorScheduledScanner {

    private static final IOSEmulatorScheduledScanner SINGLE_INSTANCE = new IOSEmulatorScheduledScanner();

    private final ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();

    private Set<IOSEmulator> lastEmulators = new HashSet<>();
    private Set<IOSEmulator> currEmulators;

    private volatile boolean running = false;

    private IOSEmulatorScheduledScanner() {
    }

    static IOSEmulatorScheduledScanner getInstance() {
        return SINGLE_INSTANCE;
    }

    synchronized void start(Duration scanPeriod, DeviceChangeListener listener) {
        if (running)
            throw new IllegalStateException("scanner is running");

        if (scanPeriod == null) scanPeriod = Duration.ofSeconds(30);
        long scanPeriodMs = scanPeriod.toMillis();

        scheduledService.scheduleAtFixedRate(() -> {
            currEmulators = getEmulatorsQuietly();

            currEmulators.stream()
                    .filter(emulator -> !lastEmulators.contains(emulator))
                    .forEach(listener::online);
            lastEmulators.stream()
                    .filter(emulator -> !currEmulators.contains(emulator))
                    .forEach(listener::offline);

            lastEmulators = currEmulators;
        }, 0, scanPeriodMs, TimeUnit.MILLISECONDS);

        running = true;
    }

    synchronized void stop() {
        if (running) {
            scheduledService.shutdown();
            running = false;
        }
    }

    private Set<IOSEmulator> getEmulatorsQuietly() {
        String cmd = "xcrun simctl list devices |grep Booted";
        try {
            String res = Terminal.execute(cmd);
            if (StringUtils.isNotEmpty(res)) {
                String[] rows = res.split("\\r?\\n");
                return Stream.of(rows).map(row -> {
                    row = row.trim(); // iPhone 11 (9CC9EA0E-86E9-4B08-9E0A-32290F96EC5F) (Booted)
                    int l = row.indexOf('(');
                    int r = row.indexOf(')');
                    String model = row.substring(0, l - 1);
                    String udid = row.substring(l + 1, r);
                    return new IOSEmulator(model, udid);
                }).collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.error("execute '{}' err", cmd, e);
        }

        return new HashSet<>();
    }
}
