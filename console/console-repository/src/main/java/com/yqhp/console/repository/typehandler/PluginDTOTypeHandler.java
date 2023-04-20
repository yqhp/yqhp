package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.PluginDTO;

/**
 * @author jiangyitao
 */
public class PluginDTOTypeHandler extends JacksonTypeHandler<PluginDTO> {
    @Override
    protected TypeReference<PluginDTO> typeReference() {
        return new TypeReference<>() {
        };
    }
}
