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
import com.yqhp.agent.web.config.Properties;
import com.yqhp.common.commons.util.HttpUtils;
import com.yqhp.console.repository.enums.ViewType;
import io.appium.java_client.Setting;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.openqa.selenium.net.UrlChecker;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
@Slf4j
public abstract class IOSDeviceDriver extends DeviceDriver {

    private static final int WDA_REMOTE_PORT = 8100;
    private static final int WDA_REMOTE_MJPEG_PORT = 9100;

    @Getter
    private String wdaUrl;
    private ShutdownHookProcessDestroyer wdaDestroyer;
    private ShutdownHookProcessDestroyer wdaForwardDestroyer;
    private ShutdownHookProcessDestroyer wdaMjpegForwardDestroyer;

    public IOSDeviceDriver(IOSDevice iosDevice) {
        super(iosDevice);
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
    protected IOSDriver newAppiumDriver(URL appiumServiceURL, DesiredCapabilities capabilities) {
        // https://appium.github.io/appium-xcuitest-driver/4.33/capabilities/
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
        capabilities.setCapability(MobileCapabilityType.UDID, device.getId());
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
        capabilities.setCapability(IOSMobileCapabilityType.WEB_DRIVER_AGENT_URL, runWdaIfNeeded());

        if (capabilities.getCapability("skipLogCapture") == null) {
            capabilities.setCapability("skipLogCapture", true);
        }

        IOSDriver iosDriver = new IOSDriver(appiumServiceURL, capabilities);
        iosDriver.setSetting(Setting.WAIT_FOR_IDLE_TIMEOUT, 0);
        return iosDriver;
    }

    /**
     * 远程真机操作不依赖于Appium IOSDriver, 提供单独创建wda session方法
     */
    public String createWdaSession() {
        String sessionId = IOSUtils.createWdaSession(wdaUrl);
        setSessionId(sessionId);
        return sessionId;
    }

    public synchronized String runWdaIfNeeded() {
        if (wdaIsRunning()) {
            return wdaUrl;
        }

        try {
            log.info("[ios][{}]run wda, bundleId={}", device.getId(), Properties.getWdaBundleId());
            wdaDestroyer = IOSUtils.runWda(device.getId(), Properties.getWdaBundleId());

            int wdaLocalPort = LocalPortProvider.getWdaAvailablePort();
            log.info("[ios][{}]forward {} -> {}", device.getId(), wdaLocalPort, WDA_REMOTE_PORT);
            wdaForwardDestroyer = IOSUtils.forward(device.getId(), wdaLocalPort, WDA_REMOTE_PORT);

            String wdaLocalUrl = "http://localhost:" + wdaLocalPort;
            String checkUrl = wdaLocalUrl + "/status";
            log.info("[ios][{}]check wda status, checkUrl={}", device.getId(), checkUrl);
            new UrlChecker().waitUntilAvailable(30, TimeUnit.SECONDS, new URL(checkUrl));
            log.info("[ios][{}]wda is running", device.getId());
            wdaUrl = wdaLocalUrl;
            return wdaUrl;
        } catch (Exception e) {
            throw new RuntimeException("run wda failed. device=" + device.getId(), e);
        }
    }

    public String getWdaMjpegUrl() {
        try {
            int mjpegLocalPort = LocalPortProvider.getWdaMjpegAvailablePort();
            log.info("[ios][{}]forward {} -> {}", device.getId(), mjpegLocalPort, WDA_REMOTE_MJPEG_PORT);
            wdaMjpegForwardDestroyer = IOSUtils.forward(device.getId(), mjpegLocalPort, WDA_REMOTE_MJPEG_PORT);
            return "http://localhost:" + mjpegLocalPort;
        } catch (Exception e) {
            throw new RuntimeException("get wda mjpeg url failed. device=" + device.getId(), e);
        }
    }

    private boolean wdaIsRunning() {
        return StringUtils.hasText(wdaUrl) && HttpUtils.isUrlAvailable(wdaUrl + "/status");
    }

    public synchronized void releaseWda() {
        if (wdaDestroyer != null) {
            log.info("[ios][{}]destroy wda", device.getId());
            wdaDestroyer.run();
            wdaDestroyer = null;
        }
        if (wdaForwardDestroyer != null) {
            log.info("[ios][{}]destroy wda forward", device.getId());
            wdaForwardDestroyer.run();
            wdaForwardDestroyer = null;
        }
        if (wdaMjpegForwardDestroyer != null) {
            log.info("[ios][{}]destroy wda mjpeg forward", device.getId());
            wdaMjpegForwardDestroyer.run();
            wdaMjpegForwardDestroyer = null;
        }
        wdaUrl = null;
    }

    @Override
    public void release() {
        super.release();
        releaseWda();
    }
}
