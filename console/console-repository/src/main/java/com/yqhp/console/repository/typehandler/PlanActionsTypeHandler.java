package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.PlanAction;

import java.util.List;

/**
 * @author jiangyitao
 */
public class PlanActionsTypeHandler extends JacksonTypeHandler<List<PlanAction>> {
    @Override
    protected TypeReference<List<PlanAction>> typeReference() {
        return new TypeReference<>() {
        };
    }
}
