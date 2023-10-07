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
package com.yqhp.agent.driver;

import com.yqhp.agent.common.LocalPortProvider;
import com.yqhp.agent.devicediscovery.ios.IOSDevice;
import com.yqhp.agent.iostools.IOSUtils;
import com.yqhp.agent.iostools.WdaUtils;
import com.yqhp.agent.web.config.Properties;
import com.yqhp.console.repository.enums.ViewType;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
@Slf4j
public class IOSDeviceDriver extends DeviceDriver {

    private static final int WDA_REMOTE_PORT = 8100;
    private static final int WDA_REMOTE_MJPEG_PORT = 9100;

    @Getter
    private String wdaUrl;
    /**
     * 用于直接向wda发送请求，而不经过appiumServer转发
     */
    @Getter
    private String wdaSessionId;
    private ShutdownHookProcessDestroyer wdaDestroyer;
    private ShutdownHookProcessDestroyer wdaForwardDestroyer;
    private ShutdownHookProcessDestroyer wdaMjpegForwardDestroyer;

    public IOSDeviceDriver(IOSDevice iosDevice) {
        super(iosDevice);
    }

    @Override
    public void installApp(File app, String... extraArgs) {
        IOSUtils.installApp(device.getId(), app);
    }

    @Override
    protected ViewType viewType() {
        return ViewType.iOS_NATIVE;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        DeviceInfo info = new DeviceInfo();
        info.setBrand("Apple");
        info.setManufacturer("Apple");

        Map fullInfo = IOSUtils.getDeviceInfo(device.getId());
        if (fullInfo != null) {
            info.setModel((String) fullInfo.get("ProductType"));
            info.setSystemVersion((String) fullInfo.get("ProductVersion"));
        }
        return info;
    }

    @Override
    public void receiveDeviceLog(Consumer<String> consumer) {
        // TODO
    }

    @Override
    public void stopReceiveDeviceLog() {
        // TODO
    }

    @Override
    protected RemoteWebDriver newWebDriver() {
        // https://appium.github.io/appium-xcuitest-driver/4.33/capabilities/
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
        capabilities.setCapability(MobileCapabilityType.UDID, device.getId());
        if (capabilities.getCapability(MobileCapabilityType.AUTOMATION_NAME) == null) {
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
        }
        if (capabilities.getCapability(IOSMobileCapabilityType.WEB_DRIVER_AGENT_URL) == null) {
            capabilities.setCapability(IOSMobileCapabilityType.WEB_DRIVER_AGENT_URL, runWdaIfNeeded());
        }
        if (capabilities.getCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT) == null) {
            capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60 * 60 * 24); // seconds
        }
        if (capabilities.getCapability("skipLogCapture") == null) {
            capabilities.setCapability("skipLogCapture", true);
        }

        IOSDriver iosDriver = new IOSDriver(getOrStartDriverService().getUrl(), capabilities);
        // wdaSessionId 与 driver.getSessionId() 不一样
        wdaSessionId = WdaUtils.getSessionId(wdaUrl);
        log.info("[ios][{}]IOSDriver wdaSessionId={}", device.getId(), wdaSessionId);
        iosDriver.setSettings(defaultWdaSettings());
        return iosDriver;
    }

    /**
     * 远程真机操作不走appiumServer(不用appiumDriver)，而是直接发送请求到wda，在此提供创建wda session方法
     * 由于wda不支持多session，如new IOSDriver，将会覆盖此处创建的session，所以在new IOSDriver后需要重新赋值wdaSessionId
     */
    public void createWdaSession() {
        wdaSessionId = WdaUtils.createSession(wdaUrl, Map.of());
        log.info("[ios][{}]Manual wdaSessionId={}", device.getId(), wdaSessionId);
    }

    /**
     * https://appium.github.io/appium-xcuitest-driver/4.33/settings/
     */
    public Map<String, Object> defaultWdaSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("waitForIdleTimeout", 0);
        settings.put("animationCoolOffTimeout", 0);
        settings.put("mjpegServerFramerate", 15); // wda视频流每秒截图次数 | 1~60 | 默认10
        settings.put("mjpegScalingFactor", 30); // wda视频流截图缩放比 | 1~100 | 默认100
        settings.put("mjpegServerScreenshotQuality", 10); // wda视频流截图质量 | 1~100 | 默认25
        return settings;
    }

    public synchronized String runWdaIfNeeded() {
        if (WdaUtils.isRunning(wdaUrl)) {
            return wdaUrl;
        }

        try {
            log.info("[ios][{}]Run wda, bundleId={}", device.getId(), Properties.getWdaBundleId());
            wdaDestroyer = WdaUtils.run(device.getId(), Properties.getWdaBundleId());

            int wdaLocalPort = LocalPortProvider.getWdaAvailablePort();
            log.info("[ios][{}]Wda forward {} -> {}", device.getId(), wdaLocalPort, WDA_REMOTE_PORT);
            wdaForwardDestroyer = IOSUtils.forward(device.getId(), wdaLocalPort, WDA_REMOTE_PORT);

            // 检查wda是否成功启动
            long timeout = System.currentTimeMillis() + 20_000;
            String wdaLocalUrl = "http://localhost:" + wdaLocalPort;
            while (!WdaUtils.isRunning(wdaLocalUrl)) {
                log.info("[ios][{}]Wda is not running now", device.getId());
                if (System.currentTimeMillis() > timeout) {
                    throw new IllegalStateException("Timeout to check wda status");
                }
                Thread.sleep(500);
            }
            log.info("[ios][{}]Wda is running now", device.getId());

            wdaUrl = wdaLocalUrl;
            return wdaUrl;
        } catch (Exception e) {
            throw new RuntimeException("Run wda failed. device=" + device.getId(), e);
        }
    }

    public String forwardWdaMjpeg() {
        try {
            int mjpegLocalPort = LocalPortProvider.getWdaMjpegAvailablePort();
            log.info("[ios][{}]WdaMjpeg forward {} -> {}", device.getId(), mjpegLocalPort, WDA_REMOTE_MJPEG_PORT);
            wdaMjpegForwardDestroyer = IOSUtils.forward(device.getId(), mjpegLocalPort, WDA_REMOTE_MJPEG_PORT);
            return "http://localhost:" + mjpegLocalPort;
        } catch (Exception e) {
            throw new RuntimeException("Forward wdaMjpeg failed. device=" + device.getId(), e);
        }
    }

    public synchronized void releaseWda() {
        if (wdaDestroyer != null) {
            log.info("[ios][{}]Destroy wda", device.getId());
            wdaDestroyer.run();
            wdaDestroyer = null;
        }
        if (wdaForwardDestroyer != null) {
            log.info("[ios][{}]Destroy wda forward", device.getId());
            wdaForwardDestroyer.run();
            wdaForwardDestroyer = null;
        }
        if (wdaMjpegForwardDestroyer != null) {
            log.info("[ios][{}]Destroy wdaMjpeg forward", device.getId());
            wdaMjpegForwardDestroyer.run();
            wdaMjpegForwardDestroyer = null;
        }
        wdaUrl = null;
        wdaSessionId = null;
    }

    @Override
    public void release() {
        super.release();
        releaseWda();
    }
}
