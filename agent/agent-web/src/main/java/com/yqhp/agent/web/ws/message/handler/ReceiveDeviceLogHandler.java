package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class ReceiveDeviceLogHandler extends DefaultInputHandler {

    public ReceiveDeviceLogHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.RECEIVE_DEVICE_LOG;
    }

    @Override
    protected void handle(Input input) {
        String uid = input.getUid();
        deviceDriver.receiveDeviceLog(log -> os.info(uid, log));
        os.ok(uid, "receiving");
    }
}
