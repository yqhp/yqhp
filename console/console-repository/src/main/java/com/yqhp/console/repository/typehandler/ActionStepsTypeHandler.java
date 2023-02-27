package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.ActionStep;

import java.util.List;

/**
 * @author jiangyitao
 */
public class ActionStepsTypeHandler extends JacksonTypeHandler<List<ActionStep>> {
    @Override
    protected TypeReference<List<ActionStep>> typeReference() {
        return new TypeReference<>() {
        };
    }
}
