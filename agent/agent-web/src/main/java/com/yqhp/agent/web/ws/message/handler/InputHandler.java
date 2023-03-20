package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;

public abstract class InputHandler<T> {
    protected abstract Command command();

    protected abstract void handle(Input<T> input) throws Exception;
}
