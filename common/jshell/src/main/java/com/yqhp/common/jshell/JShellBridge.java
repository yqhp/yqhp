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

import jdk.jshell.JShell;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jiangyitao
 */
@Slf4j
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

        String varTypeName = var.getClass().getTypeName(); // eg. com.yqhp.agent.jshell.YQHP
        vars.put(varTypeName, var);
        String toEval = "import " + varTypeName + ";";
        log.info("[inject]{}", toEval);
        jshell.eval(toEval);

        String varTypeSimpleName = var.getClass().getSimpleName(); // eg. YQHP
        toEval = String.format("%s %s = (%s) %s.getVar(%s, \"%s\");",
                varTypeSimpleName, varName, varTypeSimpleName, JSHELL_BRIDGE_CLASS_FULL_NAME, bridgeId, varTypeName);
        log.info("[inject]{}", toEval);
        jshell.eval(toEval);
    }

    public static Object getVar(Integer bridgeId, String varTypeName) {
        Map<String, Object> vars = VARS_CONTAINER.get(bridgeId);
        return vars == null ? null : vars.get(varTypeName);
    }
}
