package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.ActionDTO;

/**
 * @author jiangyitao
 */
public class ActionDTOTypeHandler extends JacksonTypeHandler<ActionDTO> {
    @Override
    protected TypeReference<ActionDTO> typeReference() {
        return new TypeReference<>() {
        };
    }
}
