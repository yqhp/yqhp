package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.entity.Plan;

/**
 * @author jiangyitao
 */
public class PlanTypeHandler extends JacksonTypeHandler<Plan> {
    @Override
    protected TypeReference<Plan> typeReference() {
        return new TypeReference<>() {
        };
    }
}
