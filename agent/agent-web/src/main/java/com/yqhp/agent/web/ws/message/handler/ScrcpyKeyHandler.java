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

import com.yqhp.agent.driver.AndroidDeviceDriver;
import com.yqhp.agent.scrcpy.ScrcpyControlClient;
import com.yqhp.agent.scrcpy.message.KeyEvent;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

import java.io.IOException;

/**
 * @author jiangyitao
 */
public class ScrcpyKeyHandler extends InputHandler<KeyEvent> {

    private final ScrcpyControlClient client;

    public ScrcpyKeyHandler(AndroidDeviceDriver driver) {
        client = driver.getScrcpy().getScrcpyControlClient();
    }

    @Override
    protected Command command() {
        return Command.SCRCPY_KEY;
    }

    @Override
    protected void handle(Input<KeyEvent> input) throws IOException {
        client.sendKeyEvent(input.getData());
    }

}
