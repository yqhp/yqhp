package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum ActionStepErrorHandler implements BaseEnum<Integer> {

    THROW_ERROR(1),
    IGNORE_ERROR(2)
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    ActionStepErrorHandler(Integer value) {
        this.value = value;
    }
}
