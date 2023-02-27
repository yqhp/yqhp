package com.yqhp.agent.web;

import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.agent.devicediscovery.android.AndroidDeviceChangeListener;
import com.yqhp.agent.devicediscovery.android.AndroidDeviceDiscovery;
import com.yqhp.agent.devicediscovery.ios.IOSEmulatorDiscovery;
import com.yqhp.agent.devicediscovery.ios.IOSRealDeviceChangeListener;
import com.yqhp.agent.devicediscovery.ios.IOSRealDeviceDiscovery;
import com.yqhp.agent.web.config.prop.AgentProperties;
import com.yqhp.agent.web.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jiangyitao
 */
@Slf4j
@Component
public class AgentLifecycle implements SmartLifecycle {

    @Autowired
    private AgentProperties agentProperties;
    @Autowired
    private DeviceService deviceService;

    private AndroidDeviceDiscovery androidDeviceDiscovery;
    private IOSRealDeviceDiscovery iosRealDeviceDiscovery;
    private IOSEmulatorDiscovery iosEmulatorDiscovery;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        if (!running.compareAndSet(false, true))
            return;

        if (agentProperties.getAndroid().isEnabled()) {
            log.info("Start to discover android device");
            androidDeviceDiscovery = new AndroidDeviceDiscovery(
                    agentProperties.getAndroid().getAdbPath(),
                    Duration.ofMinutes(2)
            );
            androidDeviceDiscovery.start(new AndroidDeviceChangeListener() {
                @Override
                public void online(Device device) {
                    log.info("[android][online] {}", device);
                    deviceService.online(device);
                }

                @Override
                public void offline(Device device) {
                    log.info("[android][offline] {}", device);
                    deviceService.offline(device.getId());
                }
            });
        }

        if (agentProperties.getIOS().getRealDevice().isEnabled()) {
            log.info("Start to discover iOS real device");
            iosRealDeviceDiscovery = new IOSRealDeviceDiscovery();
            iosRealDeviceDiscovery.start(new IOSRealDeviceChangeListener() {
                @Override
                public void online(Device device) {
                    log.info("[ios-real-device][online] {}", device);
                    deviceService.online(device);
                }

                @Override
                public void offline(Device device) {
                    log.info("[ios-real-device][offline] {}", device);
                    deviceService.offline(device.getId());
                }
            });
        }

        if (agentProperties.getIOS().getEmulator().isEnabled()) {
            log.info("Start to discover iOS emulator");
            iosEmulatorDiscovery = new IOSEmulatorDiscovery(
                    agentProperties.getIOS().getEmulator().getScanPeriod()
            );
            iosEmulatorDiscovery.start(new DeviceChangeListener() {
                @Override
                public void online(Device device) {
                    log.info("[ios-emulator][online] {}", device);
                    deviceService.online(device);
                }

                @Override
                public void offline(Device device) {
                    log.info("[ios-emulator][offline] {}", device);
                    deviceService.offline(device.getId());
                }
            });
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        if (androidDeviceDiscovery != null) {
            log.info("Stop to discover android device");
            androidDeviceDiscovery.stop();
        }
        if (iosRealDeviceDiscovery != null) {
            log.info("Stop to discover iOS real device");
            iosRealDeviceDiscovery.stop();
        }
        if (iosEmulatorDiscovery != null) {
            log.info("Stop to discover iOS emulator");
            iosEmulatorDiscovery.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}

