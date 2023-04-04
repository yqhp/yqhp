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
