package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.OutputMessageSender;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
abstract class CommandHandler<T> {

    protected final Session session;
    protected final OutputMessageSender output;
    protected final DeviceDriver deviceDriver;

    CommandHandler(Session session, DeviceDriver deviceDriver) {
        this.session = session;
        output = new OutputMessageSender(session, command());
        this.deviceDriver = deviceDriver;
    }

    protected abstract Command command();

    protected abstract void handle(String uid, T data) throws Exception;
}
