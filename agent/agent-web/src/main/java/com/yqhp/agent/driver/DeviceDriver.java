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
import com.yqhp.agent.web.config.Properties;
import com.yqhp.common.commons.util.FileUtils;
import com.yqhp.common.jshell.JShellContext;
import com.yqhp.console.repository.enums.ViewType;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.remote.SupportsContextSwitching;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.service.DriverService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
@Slf4j
public abstract class DeviceDriver extends SeleniumDriver {

    private OutputStream appiumLogOutput;
    protected final Device device;

    public DeviceDriver(Device device) {
        this.device = device;
    }

    public String getDeviceId() {
        return device.getId();
    }

    @Override
    protected void injectVar(JShellContext jshellCtx) {
        jshellCtx.injectVar(new com.yqhp.agent.jshell.Device(this));
    }

    public void installApp(String uri, String... extraArgs) throws IOException {
        File app = uri.startsWith("http")
                ? FileUtils.downloadIfAbsent(uri, new File(Properties.getDownloadDir()))
                : new File(uri);
        installApp(app, extraArgs);
    }

    public void installApp(File app, String... extraArgs) {
        ((InteractsWithApps) getOrCreateWebDriver()).installApp(app.getAbsolutePath());
    }

    protected abstract ViewType viewType();

    public boolean isNativeContext() {
        String context = ((SupportsContextSwitching) getOrCreateWebDriver()).getContext();
        return "NATIVE_APP".equals(context);
    }

    public Hierarchy dumpHierarchy() {
        Hierarchy hierarchy = new Hierarchy();
        hierarchy.setNative(isNativeContext());
        if (hierarchy.isNative()) {
            String pageSource = getOrCreateWebDriver().getPageSource();
            hierarchy.setPageSource(pageSource);
            hierarchy.setViewType(viewType());
        }
        return hierarchy;
    }

    public abstract DeviceInfo getDeviceInfo();

    public abstract void receiveDeviceLog(Consumer<String> consumer);

    public abstract void stopReceiveDeviceLog();

    @Override
    protected DriverService startDriverService() {
        int port = LocalPortProvider.getAppiumServiceAvailablePort();
        AppiumServiceBuilder serviceBuilder = new AppiumServiceBuilder()
                .usingPort(port).withIPAddress("127.0.0.1")
                .withTimeout(Duration.ofMinutes(1))
                .withArgument(GeneralServerFlag.RELAXED_SECURITY)
                .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                .withArgument(GeneralServerFlag.LOG_TIMESTAMP)
                .withArgument(GeneralServerFlag.LOCAL_TIMEZONE);
//                .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
        String appiumJsPath = Properties.getAppiumJsPath();
        if (StringUtils.isNotBlank(appiumJsPath)) {
            serviceBuilder.withAppiumJS(new File(appiumJsPath));
        }
        AppiumDriverLocalService service = AppiumDriverLocalService.buildService(serviceBuilder);
        log.info("[{}]Start appiumService: {}", device.getId(), service.getUrl());
        service.start();
        log.info("[{}]AppiumService started", device.getId());
        return service;
    }

    public synchronized void receiveAppiumLog(Consumer<String> consumer) {
        if (appiumLogOutput != null) {
            throw new IllegalStateException("Receiving");
        }

        log.info("[{}]Receive appiumLog", device.getId());
        appiumLogOutput = new OutputStream() {
            // 无需close
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
        ((AppiumDriverLocalService) getOrStartDriverService()).addOutPutStream(appiumLogOutput);
    }

    public synchronized void stopReceiveAppiumLog() {
        if (appiumLogOutput != null) {
            log.info("[{}]Stop receive appiumLog", device.getId());
            if (driverService != null) {
                ((AppiumDriverLocalService) driverService).removeOutPutStream(appiumLogOutput);
            }
            try {
                appiumLogOutput.close();
            } catch (IOException e) {
                log.error("Close appiumLogOutput failed", e);
            }
            appiumLogOutput = null;
        }
    }

    @Override
    public void release() {
        stopReceiveDeviceLog();
        stopReceiveAppiumLog();
        super.release();
    }
}
