package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.ActionX;

/**
 * @author jiangyitao
 */
public class ActionXTypeHandler extends JacksonTypeHandler<ActionX> {
    @Override
    protected TypeReference<ActionX> typeReference() {
        return new TypeReference<>() {
        };
    }
}
