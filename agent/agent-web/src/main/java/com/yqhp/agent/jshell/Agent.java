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

import com.yqhp.agent.driver.Driver;
import com.yqhp.agent.web.config.Properties;
import com.yqhp.common.commons.exception.TimeoutException;
import com.yqhp.common.commons.util.FileUtils;
import com.yqhp.common.jshell.JShellVar;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.File;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
 * @author jiangyitao
 */
@Slf4j
public class Agent implements JShellVar {

    protected final Driver driver;

    public Agent(Driver driver) {
        Assert.notNull(driver, "driver cannot be null");
        this.driver = driver;
    }

    @Override
    public String getName() {
        return "agent";
    }

    /**
     * 异步任务，统一使用该方法执行
     *
     * @since 0.0.1
     */
    public void runAsync(Runnable runnable) {
        driver.runAsync(runnable);
    }

    /**
     * 下载文件，对于相同的url，只会下载一次
     *
     * @since 0.0.1
     */
    @SneakyThrows
    public File downloadFile(String url, String filename) {
        return FileUtils.downloadIfAbsent(url, filename, new File(Properties.getDownloadDir()));
    }

    /**
     * 下载文件，对于相同的url，只会下载一次
     *
     * @since 0.0.1
     */
    @SneakyThrows
    public File downloadFile(String url) {
        return FileUtils.downloadIfAbsent(url, new File(Properties.getDownloadDir()));
    }

    /**
     * 循环执行传入的supplier，超时将抛出异常
     *
     * @param timeout 执行超时时间
     * @since 0.2.5
     */
    public <T> T execute(Duration timeout, Supplier<T> supplier) {
        return execute(timeout, Duration.ofMillis(500), supplier);
    }

    /**
     * 循环执行传入的supplier，超时将抛出异常
     *
     * @param timeout  执行超时时间
     * @param interval 执行间隔时间
     * @since 0.2.5
     */
    @SneakyThrows
    public <T> T execute(Duration timeout, Duration interval, Supplier<T> supplier) {
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        long intervalMs = interval.toMillis();
        for (; ; ) {
            try {
                return supplier.get();
            } catch (Throwable ignore) {

            }
            if (System.currentTimeMillis() > endTime) {
                throw new TimeoutException("Execution timeout, timeout=" + timeout + ", interval=" + interval);
            }
            Thread.sleep(intervalMs);
        }
    }
}