package com.yqhp.agent.jshell;

import com.yqhp.agent.androidtools.AndroidUtils;
import com.yqhp.agent.devicediscovery.android.AndroidDevice;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.common.commons.util.FileUtils;
import com.yqhp.common.jshell.JShellVar;
import io.appium.java_client.AppiumDriver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
 * @author jiangyitao
 */
@Slf4j
public class YQHP implements JShellVar {

    private final DeviceDriver deviceDriver;

    public YQHP(DeviceDriver deviceDriver) {
        Assert.notNull(deviceDriver, "deviceDriver can not be null");
        this.deviceDriver = deviceDriver;
    }

    @Override
    public String getName() {
        return "yqhp";
    }

    /**
     * 异步任务，统一使用该方法执行
     *
     * @since 0.0.1
     */
    public void runAsync(Runnable runnable) {
        deviceDriver.runAsync(runnable);
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriver appiumDriver() {
        return deviceDriver.getOrCreateAppiumDriver();
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriver refreshAppiumDriver() {
        return deviceDriver.refreshAppiumDriver();
    }

    /**
     * @since 0.0.1
     */
    public YQHP cap(String key, Object value) {
        deviceDriver.setCapability(key, value);
        return this;
    }

    /**
     * 下载文件，对于相同的url，只会下载一次
     *
     * @since 0.0.1
     */
    @SneakyThrows
    public File downloadFile(String url, String filename) {
        return FileUtils.downloadIfAbsent(url, filename);
    }

    /**
     * 下载文件，对于相同的url，只会下载一次
     *
     * @since 0.0.1
     */
    @SneakyThrows
    public File downloadFile(String url) {
        return FileUtils.downloadIfAbsent(url);
    }

    /**
     * @param uri url or filePath
     * @since 0.0.1
     */
    @SneakyThrows
    public void installApp(String uri) {
        deviceDriver.installApp(uri);
    }

    /**
     * 在android设备内执行shell命令
     * 注意: 这是在设备内部执行的命令，所以不需要加"adb shell"
     *
     * @since 0.0.1
     */
    public String androidShell(String shellCommand) {
        AndroidDevice device = (AndroidDevice) deviceDriver.getDevice();
        return AndroidUtils.executeShellCommand(device.getIDevice(), shellCommand);
    }
}