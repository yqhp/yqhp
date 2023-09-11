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
package com.yqhp.agent.jshell;

import com.yqhp.agent.driver.SeleniumDriver;
import com.yqhp.common.jshell.JShellVar;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
 * @author jiangyitao
 */
@Slf4j
public class Browser implements JShellVar {

    protected SeleniumDriver driver;

    public Browser(SeleniumDriver driver) {
        this.driver = driver;
    }

    @Override
    public String getName() {
        return "browser";
    }

    /**
     * @since 0.3.4
     */
    public Browser cap(String key, Object value) {
        driver.setCapability(key, value);
        return this;
    }

    /**
     * @since 0.3.4
     */
    public ChromeDriver chromeDriver() {
        return (ChromeDriver) driver.getOrCreateWebDriver();
    }

    /**
     * @since 0.3.8
     */
    public ChromeDriver refreshChromeDriver() {
        return (ChromeDriver) driver.refreshWebDriver();
    }
}
