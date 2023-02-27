package com.yqhp.console.repository.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yqhp.common.mybatis.typehandler.JacksonTypeHandler;
import com.yqhp.console.repository.jsonfield.PlanDevice;

import java.util.List;

/**
 * @author jiangyitao
 */
public class PlanDevicesTypeHandler extends JacksonTypeHandler<List<PlanDevice>> {
    @Override
    protected TypeReference<List<PlanDevice>> typeReference() {
        return new TypeReference<>() {
        };
    }
}
