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
import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.jshell.DeviceYQHP;
import com.yqhp.agent.web.config.Properties;
import com.yqhp.common.commons.util.FileUtils;
import com.yqhp.common.jshell.JShellContext;
import com.yqhp.console.repository.enums.ViewType;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.SupportsContextSwitching;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
@Slf4j
public abstract class DeviceDriver extends Driver {

    @Getter
    protected final Device device;

    @Setter
    private DesiredCapabilities capabilities;
    @Getter
    private AppiumDriver appiumDriver;
    private AppiumDriverLocalService appiumService;
    private OutputStream appiumLogOutput;

    public DeviceDriver(Device device) {
        this.device = device;
    }

    public String getDeviceId() {
        return device.getId();
    }

    @Override
    public void injectVar(JShellContext context) {
        context.injectVar(new DeviceYQHP(this));
    }

    public void installApp(String uri) throws IOException {
        File app = uri.startsWith("http")
                ? FileUtils.downloadIfAbsent(uri, new File(Properties.getDownloadDir()))
                : new File(uri);
        installApp(app);
    }

    public void installApp(File app) {
        ((InteractsWithApps) getOrCreateAppiumDriver()).installApp(app.getAbsolutePath());
    }

    protected abstract ViewType viewType();

    public boolean isNativeContext() {
        String context = ((SupportsContextSwitching) getOrCreateAppiumDriver()).getContext();
        return "NATIVE_APP".equals(context);
    }

    public Hierarchy dumpHierarchy() {
        Hierarchy hierarchy = new Hierarchy();
        hierarchy.setNative(isNativeContext());
        if (hierarchy.isNative()) {
            String pageSource = getOrCreateAppiumDriver().getPageSource();
            hierarchy.setPageSource(pageSource);
            hierarchy.setViewType(viewType());
        }
        return hierarchy;
    }

    public String screenshotAsBase64() {
        return getOrCreateAppiumDriver().getScreenshotAs(OutputType.BASE64);
    }

    public abstract DeviceInfo getDeviceInfo();

    public abstract void receiveDeviceLog(Consumer<String> consumer);

    public abstract void stopReceiveDeviceLog();

    public synchronized AppiumDriverLocalService getOrStartAppiumService() {
        if (appiumServiceIsRunning()) {
            return appiumService;
        }
        int port = LocalPortProvider.getAppiumServiceAvailablePort();
        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .usingPort(port)
                .withIPAddress("127.0.0.1")
                .withTimeout(Duration.ofMinutes(1))
                .withArgument(GeneralServerFlag.RELAXED_SECURITY)
                .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                .withArgument(GeneralServerFlag.LOG_TIMESTAMP)
                .withArgument(GeneralServerFlag.LOCAL_TIMEZONE);
//                .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
        String appiumJsPath = Properties.getAppiumJsPath();
        if (StringUtils.isNotBlank(appiumJsPath)) {
            builder.withAppiumJS(new File(appiumJsPath));
        }
        appiumService = AppiumDriverLocalService.buildService(builder);
        log.info("[{}]starting appium service: {}", device.getId(), appiumService.getUrl());
        appiumService.start();
        log.info("[{}]start appium service completed", device.getId());
        return appiumService;
    }

    public synchronized void stopAppiumService() {
        if (appiumServiceIsRunning()) {
            log.info("[{}]stop appium service...1", device.getId());
            appiumService.stop();
            if (appiumServiceIsRunning()) {
                log.info("[{}]stop appium service...2", device.getId());
                appiumService.stop();
            }
            appiumService = null;
        }
    }

    private boolean appiumServiceIsRunning() {
        return appiumService != null && appiumService.isRunning();
    }

    public synchronized void receiveAppiumLog(Consumer<String> consumer) {
        if (appiumLogOutput != null) {
            throw new IllegalStateException("receiving");
        }

        log.info("[{}]receive appium log", device.getId());
        appiumLogOutput = new OutputStream() {

            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            @Override
            public void write(int c) {
                bos.write(c);
                if (c == '\n') {
                    consumer.accept(bos.toString());
                    bos.reset();
                }
            }
        };
        getOrStartAppiumService().addOutPutStream(appiumLogOutput);
    }

    public synchronized void stopReceiveAppiumLog() {
        if (appiumLogOutput != null) {
            log.info("[{}]stop receive appium log", device.getId());
            if (appiumService != null) {
                appiumService.removeOutPutStream(appiumLogOutput);
            }
            try {
                appiumLogOutput.close();
            } catch (IOException e) {
                log.error("close appiumLogOutput err", e);
            }
            appiumLogOutput = null;
        }
    }

    public synchronized AppiumDriver refreshAppiumDriver() {
        quitAppiumDriver();
        return getOrCreateAppiumDriver();
    }

    public void setCapability(String key, Object value) {
        if (capabilities == null) {
            capabilities = new DesiredCapabilities();
        }
        capabilities.setCapability(key, value);
    }

    public synchronized AppiumDriver getOrCreateAppiumDriver() {
        if (appiumDriver != null) {
            return appiumDriver;
        }
        if (capabilities == null) {
            capabilities = new DesiredCapabilities();
        }
        if (capabilities.getCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT) == null) {
            capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60 * 60 * 24); // seconds
        }

        log.info("[{}]creating appium driver, capabilities: {}", device.getId(), capabilities);
        appiumDriver = newAppiumDriver(getOrStartAppiumService().getUrl(), capabilities);
        log.info("[{}]create appium driver completed, capabilities: {}", device.getId(), capabilities);
        return appiumDriver;
    }

    public synchronized void quitAppiumDriver() {
        if (appiumDriver != null) {
            try {
                log.info("[{}]quit appium driver", device.getId());
                appiumDriver.quit();
            } catch (Exception e) {
                log.warn("[{}]quit appium driver err", device.getId(), e);
            }
            appiumDriver = null;
        }
    }

    protected abstract AppiumDriver newAppiumDriver(URL appiumServiceURL, DesiredCapabilities capabilities);

    @Override
    public void release() {
        stopReceiveDeviceLog();
        stopReceiveAppiumLog();
        quitAppiumDriver();
        stopAppiumService();
        capabilities = null;
        super.release();
    }
}
