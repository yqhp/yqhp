package com.yqhp.plugin.appium;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * @author jiangyitao
 */
public class AppiumDriverWrapper {

    @Getter
    private final AppiumDriver driver;

    public AppiumDriverWrapper(AppiumDriver driver) {
        Validate.notNull(driver, "driver cannot be null");
        this.driver = driver;
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriverWrapper implicitlyWait(long millis) {
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(millis));
        return this;
    }

    /**
     * @since 0.0.1
     */
    @SneakyThrows
    public AppiumDriverWrapper sleep(long millis) {
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
    public WebElement accessibilityId(String accessibilityId) {
        return find(AppiumBy.accessibilityId(accessibilityId));
    }

    /**
     * @since 0.0.1
     */
    public WebElement accessibilityId(String accessibilityId, long millis) {
        return find(AppiumBy.accessibilityId(accessibilityId), Duration.ofMillis(millis));
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
        Validate.notBlank(text, "text cannot be blank");
        return xpath("//*[@text='" + text + "']");
    }

    /**
     * @since 0.0.1
     */
    public WebElement text(String text, long millis) {
        Validate.notBlank(text, "text cannot be blank");
        return xpath("//*[@text='" + text + "']", millis);
    }

    /**
     * @since 0.0.1
     */
    public WebElement uia(String uiautomatorText) {
        return find(AppiumBy.androidUIAutomator(uiautomatorText));
    }

    /**
     * @since 0.0.1
     */
    public WebElement uia(String uiautomatorText, long millis) {
        return find(AppiumBy.androidUIAutomator(uiautomatorText), Duration.ofMillis(millis));
    }

    /**
     * @since 0.0.1
     */
    public WebElement find(By by) {
        return driver.findElement(by);
    }

    /**
     * @since 0.0.1
     */
    public WebElement find(By by, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriverWrapper back() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        return this;
    }

}
