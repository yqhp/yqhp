package com.yqhp.plugin.appium;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.SneakyThrows;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
 * @author jiangyitao
 */
public class AppiumDriverWrapper {

    private final AppiumDriver driver;

    public AppiumDriverWrapper(AppiumDriver driver) {
        Validate.notNull(driver, "driver cannot be null");
        this.driver = driver;
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriver driver() {
        return driver;
    }

    /**
     * @since 0.0.1
     */
    public boolean isAndroid() {
        return driver instanceof AndroidDriver;
    }

    /**
     * @since 0.0.1
     */
    public boolean isIOS() {
        return driver instanceof IOSDriver;
    }

    /**
     * @since 0.0.1
     */
    public AndroidDriver androidDriver() {
        return (AndroidDriver) driver;
    }

    /**
     * @since 0.0.1
     */
    public IOSDriver iOSDriver() {
        return (IOSDriver) driver;
    }

    /**
     * 隐式等待
     *
     * @since 0.0.1
     */
    public AppiumDriverWrapper implicitlyWait(Duration duration) {
        driver.manage().timeouts().implicitlyWait(duration);
        return this;
    }

    /**
     * 休眠
     *
     * @since 0.0.1
     */
    @SneakyThrows
    public AppiumDriverWrapper sleep(Duration duration) {
        Thread.sleep(duration.toMillis());
        return this;
    }

    /**
     * 通过id查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement id(String id) {
        return find(By.id(id));
    }

    /**
     * 通过id查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _id(String id) {
        return _find(By.id(id));
    }

    /**
     * 通过id查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement id(String id, Duration timeout) {
        return find(By.id(id), timeout);
    }

    /**
     * 通过id查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _id(String id, Duration timeout) {
        return _find(By.id(id), timeout);
    }

    /**
     * 通过content-desc查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement desc(String desc) {
        return find(AppiumBy.accessibilityId(desc));
    }

    /**
     * 通过content-desc查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _desc(String desc) {
        return _find(AppiumBy.accessibilityId(desc));
    }

    /**
     * 通过content-desc查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement desc(String desc, Duration timeout) {
        return find(AppiumBy.accessibilityId(desc), timeout);
    }

    /**
     * 通过content-desc查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _desc(String desc, Duration timeout) {
        return _find(AppiumBy.accessibilityId(desc), timeout);
    }

    /**
     * 通过xpath查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement xpath(String xpath) {
        return find(By.xpath(xpath));
    }

    /**
     * 通过xpath查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _xpath(String xpath) {
        return _find(By.xpath(xpath));
    }

    /**
     * 通过xpath查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement xpath(String xpath, Duration timeout) {
        return find(By.xpath(xpath), timeout);
    }

    /**
     * 通过xpath查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _xpath(String xpath, Duration timeout) {
        return _find(By.xpath(xpath), timeout);
    }

    /**
     * 通过text查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement text(String text) {
        return uia(uiSelectorText(text));
    }

    /**
     * 通过text查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _text(String text) {
        return _uia(uiSelectorText(text));
    }

    /**
     * 通过text查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement text(String text, Duration timeout) {
        return uia(uiSelectorText(text), timeout);
    }

    /**
     * 通过text查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _text(String text, Duration timeout) {
        return _uia(uiSelectorText(text), timeout);
    }

    private String uiSelectorText(String text) {
        Validate.notBlank(text, "text cannot be blank");
        return "new UiSelector().text(\"" + text + "\")";
    }

    /**
     * 通过uiautomator查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement uia(String uiautomatorText) {
        return find(AppiumBy.androidUIAutomator(uiautomatorText));
    }

    /**
     * 通过uiautomator查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _uia(String uiautomatorText) {
        return _find(AppiumBy.androidUIAutomator(uiautomatorText));
    }

    /**
     * 通过uiautomator查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement uia(String uiautomatorText, Duration timeout) {
        return find(AppiumBy.androidUIAutomator(uiautomatorText), timeout);
    }

    /**
     * 通过uiautomator查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _uia(String uiautomatorText, Duration timeout) {
        return _find(AppiumBy.androidUIAutomator(uiautomatorText), timeout);
    }

    /**
     * 查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement find(By by) {
        return driver.findElement(by);
    }

    /**
     * 查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    public WebElement find(By by, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    /**
     * 查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _find(By by) {
        try {
            return find(by);
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return null;
        }
    }

    /**
     * 查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    public WebElement _find(By by, Duration timeout) {
        try {
            return find(by, timeout);
        } catch (TimeoutException e) {
            return null;
        }
    }

    /**
     * 获取元素中心点坐标
     *
     * @since 0.0.1
     */
    public Point getCenter(WebElement element) {
        Rectangle rect = element.getRect();
        int x = rect.x + rect.width / 2;
        int y = rect.y + rect.height / 2;
        return new Point(x, y);
    }

    /**
     * 点击元素中心坐标
     *
     * @since 0.0.1
     */
    public void tap(WebElement element) {
        Point center = getCenter(element);
        tap(center.x, center.y);
    }

    /**
     * 点击元素中心坐标
     *
     * @param pause 按下时长
     * @since 0.0.1
     */
    public void tap(WebElement element, Duration pause) {
        Point center = getCenter(element);
        tap(center.x, center.y, pause);
    }

    /**
     * 点击坐标
     *
     * @since 0.0.1
     */
    public void tap(int x, int y) {
        tap(x, y, Duration.ofMillis(200));
    }

    /**
     * 点击坐标
     *
     * @param pause 按下时长
     * @since 0.0.1
     */
    public void tap(int x, int y, Duration pause) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence seq = createTapSeq(finger, x, y, pause);
        driver.perform(List.of(seq));
    }

    /**
     * 双击元素中心点
     *
     * @since 0.0.1
     */
    public void doubleTap(WebElement element) {
        Point center = getCenter(element);
        doubleTap(center.x, center.y);
    }

    /**
     * 双击元素中心点
     *
     * @param pause 每次点击按下时长
     * @param gap   两次点击间隔时间
     * @since 0.0.1
     */
    public void doubleTap(WebElement element, Duration pause, Duration gap) {
        Point center = getCenter(element);
        doubleTap(center.x, center.y, pause, gap);
    }

    /**
     * 双击坐标
     *
     * @since 0.0.1
     */
    public void doubleTap(int x, int y) {
        doubleTap(x, y, Duration.ofMillis(200), Duration.ofMillis(40));
    }

    /**
     * 双击坐标
     *
     * @param pause 每次点击按下时长
     * @param gap   两次点击间隔时间
     * @since 0.0.1
     */
    public void doubleTap(int x, int y, Duration pause, Duration gap) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence seq = createTapSeq(finger, x, y, pause)
                .addAction(new Pause(finger, gap))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, pause))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(seq));
    }

    /**
     * @param pause 按下时长
     * @since 0.0.1
     */
    private Sequence createTapSeq(PointerInput finger, int x, int y, Duration pause) {
        return new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y)) // 移动到x,y
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg())) // 鼠标左按下
                .addAction(new Pause(finger, pause)) // 暂停
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg())); // 鼠标左抬起
    }

    /**
     * 滑动屏幕
     *
     * @since 0.0.1
     */
    public void scroll(Direction direction) {
        scroll(direction, Duration.ofMillis(300));
    }

    /**
     * 滑动屏幕
     *
     * @param duration duration 从起点滑到终点持续时间
     * @since 0.0.1
     */
    public void scroll(Direction direction, Duration duration) {
        Dimension size = driver.manage().window().getSize();
        Rectangle window = new Rectangle(0, 0, size.height, size.width);
        scrollIn(window, direction, duration);
    }

    /**
     * 在容器内滑动
     *
     * @since 0.0.1
     */
    public void scrollIn(WebElement container, Direction direction) {
        scrollIn(container, direction, Duration.ofMillis(300));
    }

    /**
     * 在容器内滑动
     *
     * @param duration duration 从起点滑到终点持续时间
     * @since 0.0.1
     */
    public void scrollIn(WebElement container, Direction direction, Duration duration) {
        scrollIn(container.getRect(), direction, duration);
    }

    /**
     * 在容器内滑动
     *
     * @param duration duration 从起点滑到终点持续时间
     * @since 0.0.1
     */
    public void scrollIn(Rectangle rect, Direction direction, Duration duration) {
        int x = rect.x, y = rect.y, width = rect.width, height = rect.height;
        switch (direction) {
            case UP:
                scroll(x + width / 2, y + height / 4, x + width / 2, y + height / 2, duration);
                break;
            case DOWN:
                scroll(x + width / 2, y + height / 2, x + width / 2, y + height / 4, duration);
                break;
            case LEFT:
                scroll(x + width / 4, y + height / 2, x + width / 2, y + height / 2, duration);
                break;
            case RIGHT:
                scroll(x + width / 2, y + height / 2, x + width / 4, y + height / 2, duration);
                break;
        }
    }

    /**
     * 滑动
     *
     * @param duration duration 从起点滑到终点持续时间
     * @since 0.0.1
     */
    public void scroll(int startX, int startY, int endX, int endY, Duration duration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence seq = createScrollSeq(finger, startX, startY, endX, endY, duration);
        driver.perform(List.of(seq));
    }

    /**
     * @param duration 从起点滑到终点持续时间
     * @since 0.0.1
     */
    private Sequence createScrollSeq(PointerInput finger, int startX, int startY, int endX, int endY, Duration duration) {
        return new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(duration, PointerInput.Origin.viewport(), endX, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    }

}
