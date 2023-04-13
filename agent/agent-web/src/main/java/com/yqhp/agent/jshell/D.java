package com.yqhp.agent.jshell;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.common.jshell.JShellVar;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

import java.time.Duration;

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

    /**
     * @since 0.0.1
     */
    public D capability(String key, Object value) {
        deviceDriver.setCapability(key, value);
        return this;
    }

    /**
     * @since 0.0.1
     */
    public D init() {
        appium();
        return this;
    }

    /**
     * @since 0.0.1
     */
    public D refresh() {
        deviceDriver.refreshAppiumDriver();
        return this;
    }

    /**
     * @since 0.0.1
     */
    public D implicitlyWait(long millis) {
        appium().manage().timeouts().implicitlyWait(Duration.ofMillis(millis));
        return this;
    }

    /**
     * @since 0.0.1
     */
    @SneakyThrows
    public D sleep(long millis) {
        Thread.sleep(millis);
        return this;
    }

    /**
     * @since 0.0.1
     */
    public WebElement id(String id) {
        return appium().findElement(By.id(id));
    }

    /**
     * @since 0.0.1
     */
    public WebElement xpath(String xpath) {
        return appium().findElement(By.xpath(xpath));
    }

    /**
     * @since 0.0.1
     */
    public WebElement text(String text) {
        Assert.hasText(text, "text must has text");
        return xpath("//*[@text='" + text + "']");
    }

    /**
     * @since 0.0.1
     */
    public D back() {
        ((AndroidDriver) appium()).pressKey(new KeyEvent(AndroidKey.BACK));
        return this;
    }

    /**
     * @since 0.0.1
     */
    @SneakyThrows
    public void install(String url) {
        deviceDriver.installApp(url);
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriver appium() {
        return deviceDriver.getOrCreateAppiumDriver();
    }
}
