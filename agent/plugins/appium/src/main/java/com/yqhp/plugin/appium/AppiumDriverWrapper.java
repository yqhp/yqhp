/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.plugin.appium;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.WebElement;

import java.time.Duration;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
 * @author jiangyitao
 */
public class AppiumDriverWrapper extends RemoteWebDriverWrapper {

    public AppiumDriverWrapper(AppiumDriver driver) {
        super(driver);
    }

    /**
     * @since 0.0.1
     */
    public AppiumDriver driver() {
        return (AppiumDriver) driver;
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
     * 通过accessibilityId查找元素(找不到则抛出异常)
     *
     * @since 0.0.4
     */
    public WebElement accessibilityId(String accessibilityId) {
        return find(AppiumBy.accessibilityId(accessibilityId));
    }

    /**
     * 通过accessibilityId查找元素(找不到则返回null)
     *
     * @since 0.0.4
     */
    public WebElement _accessibilityId(String accessibilityId) {
        return _find(AppiumBy.accessibilityId(accessibilityId));
    }

    /**
     * 通过accessibilityId查找元素(找不到则抛出异常)
     *
     * @since 0.0.4
     */
    public WebElement accessibilityId(String accessibilityId, Duration timeout) {
        return find(AppiumBy.accessibilityId(accessibilityId), timeout);
    }

    /**
     * 通过accessibilityId查找元素(找不到则返回null)
     *
     * @since 0.0.4
     */
    public WebElement _accessibilityId(String accessibilityId, Duration timeout) {
        return _find(AppiumBy.accessibilityId(accessibilityId), timeout);
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
     * 通过text查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    @Override
    public WebElement text(String text) {
        return uia(uiSelectorText(text));
    }

    /**
     * 通过text查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    @Override
    public WebElement _text(String text) {
        return _uia(uiSelectorText(text));
    }

    /**
     * 通过text查找元素(找不到则抛出异常)
     *
     * @since 0.0.1
     */
    @Override
    public WebElement text(String text, Duration timeout) {
        return uia(uiSelectorText(text), timeout);
    }

    /**
     * 通过text查找元素(找不到则返回null)
     *
     * @since 0.0.1
     */
    @Override
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

}
