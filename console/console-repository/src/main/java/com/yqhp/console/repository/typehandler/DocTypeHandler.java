package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.entity.Doc;

/**
 * @author jiangyitao
 */
public class DocTypeHandler extends JacksonTypeHandler<Doc> {
    @Override
    protected TypeReference<Doc> typeReference() {
        return new TypeReference<>() {
        };
    }
}
