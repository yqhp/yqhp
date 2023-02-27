package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum PlanExecutionRecordStatus implements BaseEnum<Integer> {

    UNCOMPLETED(0),
    COMPLETED(1),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    PlanExecutionRecordStatus(Integer value) {
        this.value = value;
    }
}
