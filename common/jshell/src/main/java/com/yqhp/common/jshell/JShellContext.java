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
package com.yqhp.common.jshell;

import lombok.Getter;

import java.io.Closeable;

/**
 * @author jiangyitao
 */
public class JShellContext implements Closeable {

    @Getter
    private final JShellX jShellX;
    private final Integer jshellBridgeId;

    public JShellContext() {
        jShellX = new JShellX();
        jshellBridgeId = JShellBridge.register();
    }

    public void injectVar(JShellVar var) {
        JShellBridge.inject(jShellX.getJShell(), jshellBridgeId, var.getName(), var);
    }

    @Override
    public void close() {
        JShellBridge.unregister(jshellBridgeId);
        jShellX.close();
    }
}
