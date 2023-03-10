package com.yqhp.common.jshell;

import jdk.jshell.JShell;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jiangyitao
 */
public class JShellBridge {
    // com.yqhp.common.jshell.JShellBridge
    private static final String JSHELL_BRIDGE_CLASS_FULL_NAME = JShellBridge.class.getName();

    // bridgeId -> vars
    public static final Map<Integer, Map<String, Object>> VARS_CONTAINER = new ConcurrentHashMap<>();
    private static final AtomicInteger BRIDGE_ID = new AtomicInteger();

    public static Integer register() {
        Integer bridgeId = BRIDGE_ID.getAndIncrement();
        VARS_CONTAINER.put(bridgeId, new ConcurrentHashMap<>());
        return bridgeId;
    }

    public static void unregister(Integer bridgeId) {
        if (bridgeId == null) return;
        Map<String, Object> vars = VARS_CONTAINER.remove(bridgeId);
        if (vars != null) vars.clear();
    }

    /**
     * @param bridgeId 调用register()获取
     * @param varName
     * @param var
     */
    public static void inject(JShell jshell, Integer bridgeId, String varName, Object var) {
        Map<String, Object> vars = VARS_CONTAINER.get(bridgeId);
        if (vars == null) {
            throw new IllegalArgumentException("bridgeId=" + bridgeId + " not exists");
        }
        vars.put(varName, var);

        String varTypeName = var.getClass().getTypeName();
        String toEval = String.format("%s %s = (%s) %s.getVar(%s, \"%s\");",
                varTypeName, varName, varTypeName, JSHELL_BRIDGE_CLASS_FULL_NAME, bridgeId, varName);
        jshell.eval(toEval);
    }

    public static Object getVar(Integer bridgeId, String varName) {
        Map<String, Object> vars = VARS_CONTAINER.get(bridgeId);
        return vars == null ? null : vars.get(varName);
    }
}
