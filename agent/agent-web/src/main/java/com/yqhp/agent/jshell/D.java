package com.yqhp.agent.jshell;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.common.jshell.JShellVar;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * @author jiangyitao
 */
@Slf4j
public class D implements JShellVar {

    private boolean inited = false;
    private final DeviceDriver deviceDriver;

    public D(DeviceDriver deviceDriver) {
        Assert.notNull(deviceDriver, "deviceDriver can not be null");
        this.deviceDriver = deviceDriver;
    }

    @Override
    public String getName() {
        return "d";
    }

    public D capability(String key, Object value) {
        deviceDriver.setCapability(key, value);
        return this;
    }

    public synchronized D init() {
        if (inited) return this;
        capability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60 * 60 * 24); // seconds = 24hour
        deviceDriver.getOrCreateAppiumDriver();
        inited = true;
        return this;
    }

    public synchronized D refresh() {
        deviceDriver.refreshAppiumDriver();
        return this;
    }

    public D sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {
        }
        return this;
    }

    public D clickId(String id) {
        appiumDriver().findElement(By.id(id)).click();
        return this;
    }

    public D back() {
        ((AndroidDriver) appiumDriver()).pressKey(new KeyEvent(AndroidKey.BACK));
        return this;
    }

    public AppiumDriver appiumDriver() {
        return Optional.ofNullable(deviceDriver.getAppiumDriver())
                .orElseThrow(() -> new IllegalStateException("appiumDriver is null"));
    }
}
