package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.AndroidDeviceDriver;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.scrcpy.message.KeyEvent;
import com.yqhp.agent.web.ws.message.Command;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author jiangyitao
 */
public class ScrcpyKeyHandler extends CommandHandler<KeyEvent> {

    public ScrcpyKeyHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.SCRCPY_KEY;
    }

    @Override
    protected void handle(String uid, KeyEvent keyEvent) throws IOException {
        ((AndroidDeviceDriver) deviceDriver).getScrcpy()
                .getScrcpyControlClient().sendKeyEvent(keyEvent);
    }

}
