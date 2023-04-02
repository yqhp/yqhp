package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class JShellEvalHandler extends DefaultInputHandler<String> {

    public JShellEvalHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.JSHELL_EVAL;
    }

    @Override
    protected void handle(Input<String> input) {
        deviceDriver.jshellEval(input.getData(), (result) ->
                os.info(input.getUid(), result)
        );
    }

}
