package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.AndroidDeviceDriver;
import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author jiangyitao
 */
public class ScrcpyTextHandler extends DefaultInputHandler<String> {

    public ScrcpyTextHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.SCRCPY_TEXT;
    }

    @Override
    protected void handle(Input<String> input) throws IOException {
        ((AndroidDeviceDriver) deviceDriver).getScrcpy()
                .getScrcpyControlClient().sendTextEvent(input.getData());
    }

}
