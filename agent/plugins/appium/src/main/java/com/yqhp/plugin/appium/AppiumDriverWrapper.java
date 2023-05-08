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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static java.time.Duration.ofMillis;

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
     * 隐式等待
     *
     * @since 0.0.1
     */
    public AppiumDriverWrapper implicitlyWait(long millis) {
        driver.manage().timeouts().implicitlyWait(ofMillis(millis));
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
        return find(By.id(id), ofMillis(millis));
    }

    /**
     * @since 0.0.1
     */
    public WebElement desc(String accessibilityId) {
        return find(AppiumBy.accessibilityId(accessibilityId));
    }

    /**
     * @since 0.0.1
     */
    public WebElement desc(String accessibilityId, long millis) {
        return find(AppiumBy.accessibilityId(accessibilityId), ofMillis(millis));
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
        return find(By.xpath(xpath), ofMillis(millis));
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
        return find(AppiumBy.androidUIAutomator(uiautomatorText), ofMillis(millis));
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
    public Point getCenter(WebElement element) {
        Point point = element.getLocation(); // 左上角
        Dimension size = element.getSize(); // 元素宽高
        int x = point.getX() + size.getWidth() / 2;
        int y = point.getY() + size.getHeight() / 2;
        return new Point(x, y);
    }

    /**
     * android返回
     *
     * @since 0.0.1
     */
    public AppiumDriverWrapper back() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        return this;
    }

    /**
     * 点击元素中心坐标
     *
     * @since 0.0.1
     */
    public void tap(WebElement element) {
        Point center = getCenter(element);
        tap(center.getX(), center.getY());
    }

    /**
     * @since 0.0.1
     */
    public void tap(int x, int y) {
        tap(x, y, 200);
    }

    /**
     * @param pauseMillis 鼠标按下到抬起间隔时间
     * @since 0.0.1
     */
    public void tap(int x, int y, long pauseMillis) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence seq = createTapSeq(finger, x, y, pauseMillis);
        driver.perform(List.of(seq));
    }

    /**
     * 双击元素中心点
     *
     * @since 0.0.1
     */
    public void doubleTap(WebElement element) {
        Point center = getCenter(element);
        doubleTap(center.getX(), center.getY());
    }

    /**
     * @since 0.0.1
     */
    public void doubleTap(int x, int y) {
        doubleTap(x, y, 200, 40);
    }

    /**
     * @param downUpPauseMillis 鼠标按下到抬起间隔时间
     * @param tapGapMillis      两次点击间隔时间
     * @since 0.0.1
     */
    private void doubleTap(int x, int y, long downUpPauseMillis, long tapGapMillis) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence seq = createTapSeq(finger, x, y, downUpPauseMillis)
                .addAction(new Pause(finger, ofMillis(tapGapMillis)))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, ofMillis(downUpPauseMillis)))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(seq));
    }

    /**
     * @param downUpPauseMillis 鼠标按下到抬起间隔时间
     * @since 0.0.1
     */
    private Sequence createTapSeq(PointerInput finger, int x, int y, long downUpPauseMillis) {
        return new Sequence(finger, 1)
                .addAction(finger.createPointerMove(ofMillis(0), PointerInput.Origin.viewport(), x, y)) // 移动到x,y
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg())) // 鼠标左按下
                .addAction(new Pause(finger, ofMillis(downUpPauseMillis))) // 暂停
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg())); // 鼠标左抬起
    }

}
