package com.yqhp.agent.driver;

import com.yqhp.agent.common.LocalPortProvider;
import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.jshell.YQHP;
import com.yqhp.agent.web.service.PluginService;
import com.yqhp.common.commons.util.FileUtils;
import com.yqhp.common.jshell.JShellContext;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.web.util.ApplicationContextUtils;
import com.yqhp.console.repository.enums.ViewType;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author jiangyitao
 */
@Slf4j
public abstract class DeviceDriver {

    @Getter
    protected final Device device;

    @Setter
    private DesiredCapabilities capabilities;
    @Getter
    private AppiumDriver appiumDriver;
    private AppiumDriverLocalService appiumService;
    private ByteArrayOutputStream appiumLogBuffer;
    private OutputStream appiumLogOutput;

    private volatile JShellContext jshellContext;

    public DeviceDriver(Device device) {
        this.device = device;
    }

    public String getDeviceId() {
        return device.getId();
    }

    public void installApp(String uri) throws IOException {
        File app = uri.startsWith("http")
                ? FileUtils.downloadIfAbsent(uri)
                : new File(uri);
        installApp(app);
    }

    public abstract void installApp(File app);

    protected abstract ViewType viewType();

    public boolean isNativeContext() {
        String context = (String) getOrCreateAppiumDriver()
                .execute(DriverCommand.GET_CURRENT_CONTEXT_HANDLE).getValue();
        return "NATIVE_APP".equals(context);
    }

    public Hierarchy dumpHierarchy() {
        Hierarchy hierarchy = new Hierarchy();
        String pageSource = getOrCreateAppiumDriver().getPageSource();
        hierarchy.setPageSource(pageSource);
        hierarchy.setViewType(viewType());
        return hierarchy;
    }

    public abstract File screenshot() throws IOException;

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
//                .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
                .withArgument(GeneralServerFlag.SESSION_OVERRIDE, "")
                .withArgument(GeneralServerFlag.LOG_TIMESTAMP, "")
                .withArgument(GeneralServerFlag.LOCAL_TIMEZONE, "");
        String appiumJsPath = ApplicationContextUtils.getProperty("agent.appium.js-path");
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
            log.info("[{}]stop appium service", device.getId());
            appiumService.stop();
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
        appiumLogBuffer = new ByteArrayOutputStream();
        appiumLogOutput = new OutputStream() {
            @Override
            public void write(int c) {
                if (c == '\n') {
                    consumer.accept(appiumLogBuffer.toString());
                    appiumLogBuffer.reset();
                } else {
                    appiumLogBuffer.write(c);
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
            try {
                appiumLogBuffer.close();
            } catch (IOException e) {
                log.error("close appiumLogBuffer err", e);
            }
            appiumLogBuffer = null;
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

    public JShellContext getOrCreateJShellContext() {
        if (jshellContext == null) {
            synchronized (DeviceDriver.class) {
                if (jshellContext == null) {
                    log.info("[{}]init jshell context...", device.getId());
                    jshellContext = new JShellContext();
                    jshellContext.injectVar(new YQHP(this));
                    log.info("[{}]init jshell context completed", device.getId());
                }
            }
        }
        return jshellContext;
    }

    public JShellEvalResult jshellEval(String input) {
        return getOrCreateJShellContext().getJShellX().eval(input);
    }

    public JShellEvalResult jshellEval(String input, Consumer<JShellEvalResult> consumer) {
        return getOrCreateJShellContext().getJShellX().eval(input, consumer);
    }

    public List<JShellEvalResult> jshellAnalysisAndEval(String input) {
        return getOrCreateJShellContext().getJShellX().analysisAndEval(input);
    }

    public List<JShellEvalResult> jshellAnalysisAndEval(String input, Consumer<JShellEvalResult> consumer) {
        return getOrCreateJShellContext().getJShellX().analysisAndEval(input, consumer);
    }

    public List<String> jshellSuggestions(String input) {
        return getOrCreateJShellContext().getJShellX().suggestions(input);
    }

    public List<String> jshellDocumentation(String input) {
        return getOrCreateJShellContext().getJShellX().documentation(input);
    }

    private static final PluginService PLUGIN_SERVICE = ApplicationContextUtils.getBean(PluginService.class);

    public List<File> jshellLoadPlugin(PluginDTO plugin) throws IOException {
        List<File> files = PLUGIN_SERVICE.downloadIfAbsent(plugin);
        jshellAddToClasspath(files);
        return files;
    }

    public void jshellAddToClasspath(List<File> files) {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        for (File file : files) {
            jshellAddToClasspath(file.getAbsolutePath());
        }
    }

    public void jshellAddToClasspath(String path) {
        Assert.hasText(path, "path must has text");
        getOrCreateJShellContext().getJShellX().getJShell().addToClasspath(path);
    }

    public synchronized void closeJShellContext() {
        if (jshellContext != null) {
            log.info("[{}]close jshell context", device.getId());
            jshellContext.close();
            jshellContext = null;
        }
    }

    public void release() {
        stopReceiveDeviceLog();
        stopReceiveAppiumLog();
        quitAppiumDriver();
        stopAppiumService();
        capabilities = null;
        closeJShellContext();
    }
}
