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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
    public D capability(String key, Object value) {
        deviceDriver.setCapability(key, value);
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
        return find(By.id(id));
    }

    /**
     * @since 0.0.1
     */
    public WebElement id(String id, long millis) {
        return find(By.id(id), Duration.ofMillis(millis));
    }

    /**
     * @since 0.0.1
     */
    public WebElement xpath(String xpath) {
        return find(By.xpath(xpath));
    }

    /**
     * @since 0.0.1
     */
    public WebElement xpath(String xpath, long millis) {
        return find(By.xpath(xpath), Duration.ofMillis(millis));
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
    public WebElement text(String text, long millis) {
        Assert.hasText(text, "text must has text");
        return xpath("//*[@text='" + text + "']", millis);
    }

    /**
     * @since 0.0.1
     */
    public WebElement find(By by) {
        return appium().findElement(by);
    }

    /**
     * @since 0.0.1
     */
    public WebElement find(By by, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(appium(), timeout);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
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
}
