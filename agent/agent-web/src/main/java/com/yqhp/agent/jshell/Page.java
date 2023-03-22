package com.yqhp.agent.jshell;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

/**
 * @author jiangyitao
 */
public class Page {

    protected final D d;
    protected final AppiumDriver appiumDriver;

    /**
     * @param duration 等待元素出现超时时间
     */
    public Page(D d, Duration duration) {
        this.d = d;
        appiumDriver = d.appiumDriver();
        PageFactory.initElements(new AppiumFieldDecorator(appiumDriver, duration), this);
    }
}