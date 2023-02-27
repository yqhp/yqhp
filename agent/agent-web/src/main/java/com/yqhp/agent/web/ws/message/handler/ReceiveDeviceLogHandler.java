package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class ReceiveDeviceLogHandler extends CommandHandler {

    public ReceiveDeviceLogHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.RECEIVE_DEVICE_LOG;
    }

    @Override
    protected void handle(String uid, Object data) {
        deviceDriver.receiveDeviceLog(log -> output.info(uid, log));
        output.ok(uid, "receiving");
    }
}
