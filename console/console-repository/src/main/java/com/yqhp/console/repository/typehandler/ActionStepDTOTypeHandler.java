package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.ActionStepDTO;

/**
 * @author jiangyitao
 */
public class ActionStepDTOTypeHandler extends JacksonTypeHandler<ActionStepDTO> {
    @Override
    protected TypeReference<ActionStepDTO> typeReference() {
        return new TypeReference<>() {
        };
    }
}
