package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum ExecutionStatus implements BaseEnum<Integer> {

    TODO(0),
    STARTED(1),
    SUCCESSFUL(2),
    FAILED(3),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    ExecutionStatus(Integer value) {
        this.value = value;
    }
}
