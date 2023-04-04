package com.yqhp.common.jshell;

import jdk.jshell.execution.DirectExecutionControl;

import java.lang.reflect.Method;

/**
 * LocalExecutionControl使用其他线程执行代码片段，性能有所损耗
 *
 * @author jiangyitao
 */
public class LocalXExecutionControl extends DirectExecutionControl {

    @Override
    protected String invoke(Method doitMethod) throws Exception {
        Object res = doitMethod.invoke(null);
        return valueString(res);
    }
}
