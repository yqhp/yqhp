package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum ActionStepKind implements BaseEnum<Integer> {

    BEFORE(1),
    NORMAL(2),
    AFTER(3),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    ActionStepKind(Integer value) {
        this.value = value;
    }
}
