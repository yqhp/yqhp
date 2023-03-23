package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.jshell.JShellEvalResult;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;

import java.util.List;

/**
 * @author jiangyitao
 */
public class JShellEvalResultsTypeHandler extends JacksonTypeHandler<List<JShellEvalResult>> {
    @Override
    protected TypeReference<List<JShellEvalResult>> typeReference() {
        return new TypeReference<>() {
        };
    }
}
