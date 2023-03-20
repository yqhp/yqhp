package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class StopReceiveDeviceLogHandler extends DefaultInputHandler {

    public StopReceiveDeviceLogHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.STOP_RECEIVE_DEVICE_LOG;
    }

    @Override
    protected void handle(Input input) {
        deviceDriver.stopReceiveDeviceLog();
        os.ok(input.getUid(), "stopped");
    }

}
