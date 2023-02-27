package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.entity.Plugin;

import java.util.List;

/**
 * @author jiangyitao
 */
public class PluginsTypeHandler extends JacksonTypeHandler<List<Plugin>> {
    @Override
    protected TypeReference<List<Plugin>> typeReference() {
        return new TypeReference<>() {
        };
    }
}
