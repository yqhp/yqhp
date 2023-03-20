package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.OutputSender;

import javax.websocket.Session;

public abstract class DefaultInputHandler<T> extends InputHandler<T> {

    protected final Session session;
    protected final OutputSender os;
    protected final DeviceDriver deviceDriver;

    DefaultInputHandler(Session session, DeviceDriver deviceDriver) {
        this.session = session;
        os = new OutputSender(session, command());
        this.deviceDriver = deviceDriver;
    }

}
