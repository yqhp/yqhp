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
        appiumDriver();
        return this;
    }

    public synchronized D refresh() {
        deviceDriver.refreshAppiumDriver();
        return this;
    }

    public D sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
        return this;
    }

    public D clickId(String id) {
        appiumDriver().findElement(By.id(id)).click();
        return this;
    }

    public D clickText(String text) {
        Assert.hasText(text, "text cannot be empty");
        By by = By.xpath("//*[contains(@text,'" + text + "')]");
        appiumDriver().findElement(by).click();
        return this;
    }

    public D back() {
        ((AndroidDriver) appiumDriver()).pressKey(new KeyEvent(AndroidKey.BACK));
        return this;
    }

    public AppiumDriver appiumDriver() {
        return deviceDriver.getOrCreateAppiumDriver();
    }
}
