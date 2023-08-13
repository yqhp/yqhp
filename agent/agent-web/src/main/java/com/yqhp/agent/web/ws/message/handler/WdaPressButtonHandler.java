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
package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.IOSDeviceDriver;
import com.yqhp.agent.iostools.WdaUtils;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

/**
 * @author jiangyitao
 */
public class WdaPressButtonHandler extends InputHandler<String> {

    private final IOSDeviceDriver driver;

    public WdaPressButtonHandler(IOSDeviceDriver driver) {
        this.driver = driver;
    }

    @Override
    protected Command command() {
        return Command.WDA_PRESS_BUTTON;
    }

    @Override
    protected void handle(Input<String> input) {
        // 不走appiumServer，直接发送到wda执行
        if ("power".equals(input.getData())) {
            boolean isLocked = WdaUtils.isDeviceLocked(driver.getWdaUrl(), driver.getWdaSessionId());
            if (isLocked) {
                WdaUtils.unlockDevice(driver.getWdaUrl(), driver.getWdaSessionId());
            } else {
                WdaUtils.lockDevice(driver.getWdaUrl(), driver.getWdaSessionId());
            }
        } else {
            WdaUtils.pressButton(driver.getWdaUrl(), driver.getWdaSessionId(), input.getData());
        }
    }

}
