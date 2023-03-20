package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class ReceiveAppiumLogHandler extends DefaultInputHandler {

    public ReceiveAppiumLogHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.RECEIVE_APPIUM_LOG;
    }

    @Override
    protected void handle(Input input) {
        String uid = input.getUid();
        deviceDriver.receiveAppiumLog(log -> os.info(uid, log));
        os.ok(uid, "receiving");
    }
}
