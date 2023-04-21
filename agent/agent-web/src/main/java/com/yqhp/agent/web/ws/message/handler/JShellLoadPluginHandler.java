package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.DeviceDriver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;
import com.yqhp.console.repository.jsonfield.PluginDTO;

import javax.websocket.Session;
import java.io.IOException;

/**
 * @author jiangyitao
 */
public class JShellLoadPluginHandler extends DefaultInputHandler<PluginDTO> {

    public JShellLoadPluginHandler(Session session, DeviceDriver deviceDriver) {
        super(session, deviceDriver);
    }

    @Override
    protected Command command() {
        return Command.JSHELL_LOAD_PLUGIN;
    }

    @Override
    protected void handle(Input<PluginDTO> input) throws IOException {
        os.info(input.getUid(), "加载中...");
        deviceDriver.jshellLoadPlugin(input.getData());
        os.ok(input.getUid(), "加载完成");
    }

}
