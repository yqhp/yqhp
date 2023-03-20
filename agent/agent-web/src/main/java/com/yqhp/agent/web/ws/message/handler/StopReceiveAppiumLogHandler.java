package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class StopReceiveAppiumLogHandler extends DefaultInputHandler {

    public StopReceiveAppiumLogHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.STOP_RECEIVE_APPIUM_LOG;
    }

    @Override
    protected void handle(Input input) {
        deviceDriver.stopReceiveAppiumLog();
        os.ok(input.getUid(), "stopped");
    }
}
