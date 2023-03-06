package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.ActionStepX;

/**
 * @author jiangyitao
 */
public class ActionStepXTypeHandler extends JacksonTypeHandler<ActionStepX> {
    @Override
    protected TypeReference<ActionStepX> typeReference() {
        return new TypeReference<>() {
        };
    }
}
