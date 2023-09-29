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
import com.yqhp.agent.driver.SeleniumDriver;
import com.yqhp.common.commons.util.JacksonUtils;
import com.yqhp.common.jshell.JShellVar;
import com.yqhp.common.web.util.ApplicationContextUtils;
import com.yqhp.common.web.util.MultipartFileUtils;
import com.yqhp.console.repository.jsonfield.DocExecutionLog;
import com.yqhp.file.model.OSSFile;
import com.yqhp.file.rpc.FileRpc;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Map;

/**
 * 为了减轻调用方负担，这里对外提供的api，都不抛受检异常，使用@SneakyThrows自动抛出非受检异常
 *
 * @author jiangyitao
 */
@Slf4j
public class Logger implements JShellVar {

    private static final FileRpc FILE_RPC = ApplicationContextUtils.getBean(FileRpc.class);

    private final Driver driver;

    public Logger(Driver driver) {
        Assert.notNull(driver, "driver cannot be null");
        this.driver = driver;
    }

    @Override
    public String getName() {
        return "log";
    }

    /**
     * @since 0.3.2
     */
    public void info(Object obj) {
        driver.log(createLog("info", obj));
    }

    /**
     * @since 0.3.2
     */
    public void warn(Object obj) {
        driver.log(createLog("warn", obj));
    }

    /**
     * @since 0.3.2
     */
    public void error(Object obj) {
        driver.log(createLog("error", obj));
    }

    /**
     * 仅适用于UI自动化
     * 非UI自动化调用将抛出异常，如接口自动化
     *
     * @since 0.3.5
     */
    public void screenshot() {
        screenshot("");
    }

    /**
     * 仅适用于UI自动化
     * 非UI自动化调用将抛出异常，如接口自动化
     *
     * @since 0.3.5
     */
    @SneakyThrows
    public void screenshot(String info) {
        if (!(driver instanceof SeleniumDriver)) {
            throw new UnsupportedOperationException();
        }
        File screenshot = null;
        try {
            screenshot = ((SeleniumDriver) driver).screenshotAs(OutputType.FILE);
            OSSFile ossFile = FILE_RPC.uploadFile(MultipartFileUtils.toMultipartFile(screenshot), false);
            String val = JacksonUtils.writeValueAsString(Map.of("info", info, "file", ossFile));
            driver.log(createLog("screenshot", val));
        } finally {
            if (screenshot != null && !screenshot.delete()) {
                log.warn("Failed to delete " + screenshot);
            }
        }
    }

    /**
     * @since 1.1.7
     */
    public void video(File file) {
        video(file, "");
    }

    /**
     * @since 1.1.7
     */
    @SneakyThrows
    public void video(File file, String info) {
        if (file == null) {
            return;
        }
        try {
            OSSFile ossFile = FILE_RPC.uploadFile(MultipartFileUtils.toMultipartFile(file), false);
            String val = JacksonUtils.writeValueAsString(Map.of("info", info, "file", ossFile));
            driver.log(createLog("video", val));
        } finally {
            if (!file.delete()) {
                log.warn("Failed to delete " + file);
            }
        }
    }

    private DocExecutionLog createLog(String tag, Object obj) {
        DocExecutionLog executionLog = new DocExecutionLog();
        executionLog.setTimestamp(System.currentTimeMillis());
        executionLog.setTag(tag);
        executionLog.setValue(String.valueOf(obj));
        return executionLog;
    }

}
