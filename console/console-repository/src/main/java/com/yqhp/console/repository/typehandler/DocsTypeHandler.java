package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.entity.Doc;

import java.util.List;

/**
 * @author jiangyitao
 */
public class DocsTypeHandler extends JacksonTypeHandler<List<Doc>> {
    @Override
    protected TypeReference<List<Doc>> typeReference() {
        return new TypeReference<>() {
        };
    }
}
