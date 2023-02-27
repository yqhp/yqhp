package com.yqhp.agent.driver;

import com.yqhp.agent.common.LocalPortProvider;
import com.yqhp.agent.devicediscovery.Device;
import com.yqhp.agent.jshell.D;
import com.yqhp.common.jshell.JShellConst;
import com.yqhp.common.jshell.JShellContext;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.jshell.JShellTool;
import com.yqhp.common.web.util.ApplicationContextUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.execution.LocalExecutionControlProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    private volatile JShell jshell;
    private volatile JShellTool jshellTool;
    private JShellContext jshellContext;

    public DeviceDriver(Device device) {
        this.device = device;
    }

    public String getDeviceId() {
        return device.getId();
    }

    public abstract void installApp(File app);

    public abstract File screenshot() throws IOException;

    public abstract DeviceInfo getDeviceInfo();

    public abstract void receiveDeviceLog(Consumer<String> consumer);

    public abstract void stopReceiveDeviceLog();

    public synchronized AppiumDriverLocalService startAppiumService() {
        if (appiumServiceIsRunning()) {
            return appiumService;
        }
        int port = LocalPortProvider.getAppiumServiceAvailablePort();
        AppiumServiceBuilder builder = new AppiumServiceBuilder().usingPort(port);
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
        startAppiumService().addOutPutStream(appiumLogOutput);
    }

    public synchronized void stopReceiveAppiumLog() {
        if (appiumLogOutput != null) {
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
        return createAppiumDriver();
    }

    public void setCapability(String key, Object value) {
        if (capabilities == null) {
            capabilities = new DesiredCapabilities();
        }
        capabilities.setCapability(key, value);
    }

    public synchronized AppiumDriver createAppiumDriver() {
        if (appiumDriver != null) return appiumDriver;
        if (capabilities == null) capabilities = new DesiredCapabilities();

        log.info("[{}]creating appium driver, capabilities: {}", device.getId(), capabilities);
        appiumDriver = newAppiumDriver(startAppiumService().getUrl(), capabilities);
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

    public JShell getJShell() {
        if (jshell == null) {
            synchronized (DeviceDriver.class) {
                if (jshell == null) {
                    log.info("[{}]start initializing jshell", device.getId());
                    JShell.Builder builder = JShell.builder()
                            .executionEngine(new LocalExecutionControlProvider(), null);
                    jshell = builder.build();
                    for (String defaultImport : JShellConst.DEFAULT_IMPORTS) {
                        log.info("[{}]jshell eval: {}", device.getId(), defaultImport);
                        jshell.eval(defaultImport);
                    }
                    for (String printing : JShellConst.PRINTINGS) {
                        log.info("[{}]jshell eval: {}", device.getId(), printing);
                        jshell.eval(printing);
                    }

                    jshellContext = new JShellContext(jshell);
                    D d = new D(this);
                    log.info("[{}]jshell context injectVar: {}", device.getId(), d.getName());
                    jshellContext.injectVar(d);

                    log.info("[{}]initialize jshell completed", device.getId());
                }
            }
        }
        return jshell;
    }

    public List<JShellEvalResult> jshellEval(String input) {
        if (jshellTool == null) {
            synchronized (DeviceDriver.class) {
                if (jshellTool == null) {
                    jshellTool = new JShellTool(getJShell());
                }
            }
        }
        return jshellTool.eval(input);
    }

    public List<String> jshellCompletionSuggestions(String input) {
        if (StringUtils.isBlank(input)) return new ArrayList<>();
        return getJShell().sourceCodeAnalysis()
                .completionSuggestions(input, input.length(), new int[]{-1}).stream()
                .filter(SourceCodeAnalysis.Suggestion::matchesType)
                .map(SourceCodeAnalysis.Suggestion::continuation)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> jshellDocs(String input) {
        if (StringUtils.isBlank(input)) return new ArrayList<>();
        return getJShell().sourceCodeAnalysis()
                .documentation(input, input.length(), false).stream()
                .map(SourceCodeAnalysis.Documentation::signature)
                .collect(Collectors.toList());
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
        getJShell().addToClasspath(path);
    }

    public synchronized void closeJShell() {
        if (jshellContext != null) {
            jshellContext.close();
            jshellContext = null;
        }
        if (jshellTool != null) {
            jshellTool = null;
        }
        if (jshell != null) {
            log.info("[{}]close jshell", device.getId());
            jshell.close();
            jshell = null;
        }
    }

    public void release() {
        stopReceiveDeviceLog();
        stopReceiveAppiumLog();
        quitAppiumDriver();
        stopAppiumService();
        closeJShell();
    }
}
