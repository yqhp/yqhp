package com.yqhp.common.jshell;

import jdk.jshell.JShell;

/**
 * @author jiangyitao
 */
public class JShellContext {

    private final JShell jshell;
    private final Integer jshellBridgeId;

    public JShellContext(JShell jshell) {
        this.jshell = jshell;
        jshellBridgeId = JShellBridge.register();
    }

    public void injectVar(JShellVar var) {
        JShellBridge.inject(jshell, jshellBridgeId, var.getName(), var);
    }

    public void close() {
        JShellBridge.unregister(jshellBridgeId);
    }
}
