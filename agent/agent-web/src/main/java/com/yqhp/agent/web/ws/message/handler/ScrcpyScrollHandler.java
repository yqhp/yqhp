package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.AndroidDeviceDriver;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.scrcpy.message.ScrollEvent;
import com.yqhp.agent.web.ws.message.Command;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author jiangyitao
 */
public class ScrcpyScrollHandler extends CommandHandler<ScrollEvent> {

    public ScrcpyScrollHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.SCRCPY_SCROLL;
    }

    @Override
    protected void handle(String uid, ScrollEvent scrollEvent) throws IOException {
        ((AndroidDeviceDriver) deviceDriver).getScrcpy()
                .getScrcpyControlClient().sendScrollEvent(scrollEvent);
    }

}
