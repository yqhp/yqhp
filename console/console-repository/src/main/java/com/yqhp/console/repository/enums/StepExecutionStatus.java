package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum StepExecutionStatus implements BaseEnum<Integer> {

    TODO(0),
    RECEIVED(1),
    STARTED(2),
    SUCCESSFUL(3),
    FAILED(4),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    StepExecutionStatus(Integer value) {
        this.value = value;
    }
}
