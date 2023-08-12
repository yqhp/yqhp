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

import com.yqhp.agent.appium.WdaTouchEvent;
import com.yqhp.agent.driver.IOSDeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;
import com.yqhp.common.commons.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author jiangyitao
 */
@Slf4j
public class WdaTouchHandler extends InputHandler<WdaTouchEvent> {

    private static final PointerInput.Origin VIEW = PointerInput.Origin.viewport();
    private static final int MOUSE_LEFT = PointerInput.MouseButton.LEFT.asArg();

    private final IOSDeviceDriver driver;

    private long touchDownTime;
    private PointerInput finger;
    private Sequence seq;

    public WdaTouchHandler(IOSDeviceDriver driver) {
        this.driver = driver;
    }

    @Override
    protected Command command() {
        return Command.WDA_TOUCH;
    }

    @Override
    protected void handle(Input<WdaTouchEvent> input) {
        long timestamp = input.getTimestamp();
        WdaTouchEvent touch = input.getData();
        if (touch.getAction() == WdaTouchEvent.DOWN) {
            // 记录按下时间
            touchDownTime = timestamp;
            finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            // 移动到x,y按下
            seq = new Sequence(finger, 0);
            seq.addAction(finger.createPointerMove(Duration.ZERO, VIEW, touch.getX(), touch.getY()));
            seq.addAction(finger.createPointerDown(MOUSE_LEFT));
        } else if (touch.getAction() == WdaTouchEvent.UP) {
            if (touchDownTime == 0) { // 没有按下
                return;
            }
            Duration duration = Duration.ofMillis(timestamp - touchDownTime);
            seq.addAction(finger.createPointerMove(duration, VIEW, touch.getX(), touch.getY()));
            seq.addAction(finger.createPointerUp(MOUSE_LEFT));
            touchDownTime = 0;
            wdaPerform();
        }
    }

    /**
     * 不走appiumServer，直接发送到wda执行
     */
    private void wdaPerform() {
        String url = driver.getWdaUrl() + "/session/" + driver.getWdaSessionId() + "/actions";
        Map<String, Object> body = Map.of("actions", List.of(seq.encode()));
        HttpUtils.postJSON(url, body);
    }

}
