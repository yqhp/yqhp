package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.PluginFile;

import java.util.List;

/**
 * @author jiangyitao
 */
public class PluginFilesTypeHandler extends JacksonTypeHandler<List<PluginFile>> {
    @Override
    protected TypeReference<List<PluginFile>> typeReference() {
        return new TypeReference<>() {
        };
    }
}
