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
package com.yqhp.agent.web;

import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.devicediscovery.DeviceChangeListener;
import com.yqhp.agent.devicediscovery.DeviceDiscovery;
import com.yqhp.agent.devicediscovery.android.AndroidDeviceChangeListener;
import com.yqhp.agent.devicediscovery.android.AndroidDeviceDiscovery;
import com.yqhp.agent.devicediscovery.ios.IOSSimulatorDiscovery;
import com.yqhp.agent.devicediscovery.ios.IOSRealDeviceChangeListener;
import com.yqhp.agent.devicediscovery.ios.IOSRealDeviceDiscovery;
import com.yqhp.agent.web.config.prop.AgentProperties;
import com.yqhp.agent.web.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
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

    private DeviceDiscovery androidDeviceDiscovery;
    private DeviceDiscovery iOSRealDeviceDiscovery;
    private DeviceDiscovery iOSSimulatorDiscovery;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        if (!running.compareAndSet(false, true))
            return;

        if (agentProperties.getAndroid().isEnabled()) {
            log.info("Start to discover android device");
            String adbPath = agentProperties.getAndroid().getAdbPath();
            androidDeviceDiscovery = new AndroidDeviceDiscovery(adbPath, Duration.ofMinutes(2));
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
            iOSRealDeviceDiscovery = new IOSRealDeviceDiscovery();
            iOSRealDeviceDiscovery.start(new IOSRealDeviceChangeListener() {
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

        if (agentProperties.getIOS().getSimulator().isEnabled()) {
            Duration scanPeriod = agentProperties.getIOS().getSimulator().getScanPeriod();
            log.info("Start to discover iOS simulator, scanPeriod={}", scanPeriod);
            iOSSimulatorDiscovery = new IOSSimulatorDiscovery(scanPeriod);
            iOSSimulatorDiscovery.start(new DeviceChangeListener() {
                @Override
                public void online(Device device) {
                    log.info("[ios-simulator][online] {}", device);
                    deviceService.online(device);
                }

                @Override
                public void offline(Device device) {
                    log.info("[ios-simulator][offline] {}", device);
                    deviceService.offline(device.getId());
                }
            });
        }

        if (agentProperties.getOpencv().isEnabled()) {
            log.info("Opencv loading...");
            OpenCV.loadShared();
            log.info("Opencv loaded");
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
        if (iOSRealDeviceDiscovery != null) {
            log.info("Stop to discover iOS real device");
            iOSRealDeviceDiscovery.stop();
        }
        if (iOSSimulatorDiscovery != null) {
            log.info("Stop to discover iOS simulator");
            iOSSimulatorDiscovery.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}

