package com.yqhp.plugin.appium;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.Getter;
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
import java.util.Optional;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
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
     * @since 0.0.1
     */
    @SneakyThrows
    public AppiumDriverWrapper sleep(Duration duration) {
        Thread.sleep(duration.toMillis());
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
    public WebElement id(String id, Duration duration) {
        return find(By.id(id), duration);
    }

    /**
     * @since 0.0.1
     */
    public WebElement desc(String desc) {
        return find(AppiumBy.accessibilityId(desc));
    }

    /**
     * @since 0.0.1
     */
    public WebElement desc(String desc, Duration duration) {
        return find(AppiumBy.accessibilityId(desc), duration);
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
    public WebElement xpath(String xpath, Duration duration) {
        return find(By.xpath(xpath), duration);
    }

    /**
     * @since 0.0.1
     */
    public WebElement text(String text) {
        return uia(textUiSelector(text));
    }

    /**
     * @since 0.0.1
     */
    public WebElement text(String text, Duration duration) {
        return uia(textUiSelector(text), duration);
    }

    private String textUiSelector(String text) {
        Validate.notBlank(text, "text cannot be blank");
        return "new UiSelector().text(\"" + text + "\")";
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
    public WebElement uia(String uiautomatorText, Duration duration) {
        return find(AppiumBy.androidUIAutomator(uiautomatorText), duration);
    }

    /**
     * @since 0.0.1
     */
    public Optional<WebElement> findQuietly(By by) {
        try {
            return Optional.of(find(by));
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return Optional.empty();
        }
    }

    /**
     * @since 0.0.1
     */
    public Optional<WebElement> findQuietly(By by, Duration timeout) {
        try {
            return Optional.of(find(by, timeout));
        } catch (TimeoutException e) {
            return Optional.empty();
        }
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
     * @since 0.0.1
     */
    public void tap(int x, int y) {
        tap(x, y, Duration.ofMillis(200));
    }

    /**
     * @param downUpPauseDuration 鼠标按下到抬起间隔时间
     * @since 0.0.1
     */
    public void tap(int x, int y, Duration downUpPauseDuration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence seq = createTapSeq(finger, x, y, downUpPauseDuration);
        driver.perform(List.of(seq));
    }

    /**
     * @since 0.0.1
     */
    public void longPress(WebElement element) {
        Point center = getCenter(element);
        longPress(center.x, center.y);
    }

    /**
     * @since 0.0.1
     */
    public void longPress(int x, int y) {
        tap(x, y, Duration.ofSeconds(1));
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
     * @since 0.0.1
     */
    public void doubleTap(int x, int y) {
        doubleTap(x, y, Duration.ofMillis(200), Duration.ofMillis(40));
    }

    /**
     * @param downUpPauseDuration 鼠标按下到抬起间隔时间
     * @param tapGapDuration      两次点击间隔时间
     * @since 0.0.1
     */
    public void doubleTap(int x, int y, Duration downUpPauseDuration, Duration tapGapDuration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence seq = createTapSeq(finger, x, y, downUpPauseDuration)
                .addAction(new Pause(finger, tapGapDuration))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, downUpPauseDuration))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(seq));
    }

    /**
     * @param downUpPauseDuration 鼠标按下到抬起间隔时间
     * @since 0.0.1
     */
    private Sequence createTapSeq(PointerInput finger, int x, int y, Duration downUpPauseDuration) {
        return new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y)) // 移动到x,y
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg())) // 鼠标左按下
                .addAction(new Pause(finger, downUpPauseDuration)) // 暂停
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg())); // 鼠标左抬起
    }

    /**
     * @since 0.0.1
     */
    public void scroll(Direction direction) {
        scroll(direction, Duration.ofMillis(300));
    }

    /**
     * @since 0.0.1
     */
    public void scroll(Direction direction, Duration duration) {
        Dimension size = driver.manage().window().getSize();
        Rectangle window = new Rectangle(0, 0, size.height, size.width);
        scrollIn(window, direction, duration);
    }

    /**
     * @since 0.0.1
     */
    public void scrollIn(WebElement container, Direction direction) {
        scrollIn(container, direction, Duration.ofMillis(300));
    }

    /**
     * @since 0.0.1
     */
    public void scrollIn(WebElement container, Direction direction, Duration duration) {
        scrollIn(container.getRect(), direction, duration);
    }

    /**
     * 在容器内滚动
     *
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
     * @param duration 滑动持续时间
     * @since 0.0.1
     */
    public void scroll(int startX, int startY, int endX, int endY, Duration duration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence seq = createScrollSeq(finger, startX, startY, endX, endY, duration);
        driver.perform(List.of(seq));
    }

    /**
     * @param duration 滑动持续时间
     * @return
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
