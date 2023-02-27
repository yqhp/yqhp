package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class StopReceiveAppiumLogHandler extends CommandHandler {

    public StopReceiveAppiumLogHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.STOP_RECEIVE_APPIUM_LOG;
    }

    @Override
    protected void handle(String uid, Object data) {
        deviceDriver.stopReceiveAppiumLog();
        output.ok(uid, "stopped");
    }
}
