package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;
import com.yqhp.common.jshell.TriggerSuggestRequest;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class JShellSuggestionsHandler extends DefaultInputHandler<TriggerSuggestRequest> {

    public JShellSuggestionsHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.JSHELL_SUGGESTIONS;
    }

    @Override
    protected void handle(Input<TriggerSuggestRequest> input) {
        os.info(input.getUid(), deviceDriver.jshellSuggestions(input.getData()));
    }
}
