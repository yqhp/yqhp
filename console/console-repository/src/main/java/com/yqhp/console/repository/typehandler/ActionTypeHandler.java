package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.entity.Action;

/**
 * @author jiangyitao
 */
public class ActionTypeHandler extends JacksonTypeHandler<Action> {
    @Override
    protected TypeReference<Action> typeReference() {
        return new TypeReference<>() {
        };
    }
}
