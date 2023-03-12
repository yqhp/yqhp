package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum ActionStepType implements BaseEnum<Integer> {

    DOC_JSH_EXECUTABLE(1),
    ACTION(2),
    JSH(3),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    ActionStepType(Integer value) {
        this.value = value;
    }
}
