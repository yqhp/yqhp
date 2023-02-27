package com.yqhp.common.mybatis.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * @author jiangyitao
 */
public class StringsTypeHandler extends JacksonTypeHandler<List<String>> {
    @Override
    protected TypeReference<List<String>> typeReference() {
        return new TypeReference<>() {
        };
    }
}
