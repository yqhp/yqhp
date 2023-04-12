package com.yqhp.agent.jshell;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.common.jshell.JShellVar;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * @author jiangyitao
 */
@Slf4j
public class D implements JShellVar {

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

    public D init() {
        appium();
        return this;
    }

    public D refresh() {
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
        appium().findElement(By.id(id)).click();
        return this;
    }

    public D clickText(String text) {
        Assert.hasText(text, "text cannot be empty");
        By by = By.xpath("//*[contains(@text,'" + text + "')]");
        appium().findElement(by).click();
        return this;
    }

    public D back() {
        ((AndroidDriver) appium()).pressKey(new KeyEvent(AndroidKey.BACK));
        return this;
    }

    public void install(String url) {
        try {
            deviceDriver.installApp(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AppiumDriver appium() {
        return deviceDriver.getOrCreateAppiumDriver();
    }
}
