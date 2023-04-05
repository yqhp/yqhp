package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class JShellDocumentationHandler extends DefaultInputHandler<String> {

    public JShellDocumentationHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.JSHELL_DOCUMENTATION;
    }

    @Override
    protected void handle(Input<String> input) {
        os.info(input.getUid(), deviceDriver.jshellDocumentation(input.getData()));
    }

}
