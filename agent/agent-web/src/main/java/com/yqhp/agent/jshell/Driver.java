package com.yqhp.agent.jshell;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.common.jshell.JShellVar;
import io.appium.java_client.AppiumDriver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * @author jiangyitao
 */
@Slf4j
public class Driver implements JShellVar {

    private final DeviceDriver deviceDriver;

    public Driver(DeviceDriver deviceDriver) {
        Assert.notNull(deviceDriver, "deviceDriver can not be null");
        this.deviceDriver = deviceDriver;
    }

    @Override
    public String getName() {
        return "driver";
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriver appium() {
        return deviceDriver.getOrCreateAppiumDriver();
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriver refresh() {
        return deviceDriver.refreshAppiumDriver();
    }

    /**
     * @since 0.0.1
     */
    public Driver capability(String key, Object value) {
        deviceDriver.setCapability(key, value);
        return this;
    }

    /**
     * @since 0.0.1
     */
    @SneakyThrows
    public void install(String url) {
        deviceDriver.installApp(url);
    }
}
