package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.PluginDTO;

import java.util.List;

/**
 * @author jiangyitao
 */
public class PluginDTOsTypeHandler extends JacksonTypeHandler<List<PluginDTO>> {
    @Override
    protected TypeReference<List<PluginDTO>> typeReference() {
        return new TypeReference<>() {
        };
    }
}
