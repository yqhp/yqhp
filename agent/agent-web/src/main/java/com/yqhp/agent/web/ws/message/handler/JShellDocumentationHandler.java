package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;
import com.yqhp.common.jshell.DocumentationRequest;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class JShellDocumentationHandler extends DefaultInputHandler<DocumentationRequest> {

    public JShellDocumentationHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.JSHELL_DOCUMENTATION;
    }

    @Override
    protected void handle(Input<DocumentationRequest> input) {
        os.info(input.getUid(), deviceDriver.jshellDocumentation(input.getData()));
    }
}
