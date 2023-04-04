package com.yqhp.common.jshell;

import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

import java.util.Map;

/**
 * @author jiangyitao
 */
public class LocalXExecutionControlProvider implements ExecutionControlProvider {

    @Override
    public String name() {
        return "localX";
    }

    @Override
    public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) throws Throwable {
        return new LocalXExecutionControl();
    }
}
