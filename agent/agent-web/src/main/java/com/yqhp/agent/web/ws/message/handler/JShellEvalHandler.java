/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.agent.web.ws.message.handler;

import com.yqhp.agent.driver.Driver;
import com.yqhp.agent.web.ws.message.Command;
import com.yqhp.agent.web.ws.message.Input;
import com.yqhp.agent.web.ws.message.OutputSender;

import javax.websocket.Session;

/**
 * @author jiangyitao
 */
public class JShellEvalHandler extends InputHandler<String> {

    private final OutputSender os;
    private final Driver driver;

    public JShellEvalHandler(Session session, Driver driver) {
        os = new OutputSender(session, command());
        this.driver = driver;
    }

    @Override
    protected Command command() {
        return Command.JSHELL_EVAL;
    }

    @Override
    protected void handle(Input<String> input) {
        driver.jshellAnalysisAndEval(input.getData(), (result) ->
                os.info(input.getUid(), result)
        );
    }

}
