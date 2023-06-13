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
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.scrcpy.message.ScrollEvent;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author jiangyitao
 */
public class ScrcpyScrollHandler extends DefaultInputHandler<ScrollEvent> {

    public ScrcpyScrollHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.SCRCPY_SCROLL;
    }

    @Override
    protected void handle(Input<ScrollEvent> input) throws IOException {
        ((AndroidDeviceDriver) deviceDriver).getScrcpy()
                .getScrcpyControlClient().sendScrollEvent(input.getData());
    }

}
