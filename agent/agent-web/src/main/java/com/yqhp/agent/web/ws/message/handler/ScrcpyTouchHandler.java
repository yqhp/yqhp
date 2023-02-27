package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.AndroidDeviceDriver;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.scrcpy.message.TouchEvent;
import com.yqhp.agent.web.ws.message.Command;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author jiangyitao
 */
public class ScrcpyTouchHandler extends CommandHandler<TouchEvent> {

    public ScrcpyTouchHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.SCRCPY_TOUCH;
    }

    @Override
    protected void handle(String uid, TouchEvent touchEvent) throws IOException {
        ((AndroidDeviceDriver) deviceDriver).getScrcpy()
                .getScrcpyControlClient().sendTouchEvent(touchEvent);
    }

}
