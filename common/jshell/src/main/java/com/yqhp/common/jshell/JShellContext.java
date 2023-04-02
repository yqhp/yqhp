package com.yqhp.common.jshell;

import jdk.jshell.JShell;
import jdk.jshell.execution.LocalExecutionControlProvider;
import lombok.Getter;

/**
 * @author jiangyitao
 */
public class JShellContext {

    @Getter
    private final JShell jshell;
    @Getter
    private final JShellTool jShellTool;
    private final Integer jshellBridgeId;

    public JShellContext() {
        JShell.Builder builder = JShell.builder()
                .executionEngine(new LocalExecutionControlProvider(), null);
        JShell jshell = builder.build();
        for (String defaultImport : JShellConst.DEFAULT_IMPORTS) {
            jshell.eval(defaultImport);
        }
        for (String printing : JShellConst.PRINTINGS) {
            jshell.eval(printing);
        }
        this.jshell = jshell;
        this.jShellTool = new JShellTool(jshell);
        jshellBridgeId = JShellBridge.register();
    }

    public void injectVar(JShellVar var) {
        JShellBridge.inject(jshell, jshellBridgeId, var.getName(), var);
    }

    public void close() {
        JShellBridge.unregister(jshellBridgeId);
        jshell.close();
    }
}
