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
import com.yqhp.common.jshell.JShellVar;
import org.springframework.util.Assert;

/**
 * @author jiangyitao
 */
public class Logger implements JShellVar {

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
        driver.log(new Log("info", obj));
    }

    /**
     * @since 0.3.2
     */
    public void warn(Object obj) {
        driver.log(new Log("warn", obj));
    }

    /**
     * @since 0.3.2
     */
    public void error(Object obj) {
        driver.log(new Log("error", obj));
    }

    public static class Log {
        public long timestamp;
        public String tag;
        public String value;

        public Log(String tag, Object val) {
            timestamp = System.currentTimeMillis();
            this.tag = tag;
            this.value = String.valueOf(val);
        }
    }

}
