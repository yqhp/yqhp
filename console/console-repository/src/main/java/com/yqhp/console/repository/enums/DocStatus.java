package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DocStatus implements BaseEnum<Integer> {

    DRAFT(1),
    DISABLED(2),
    DEPRECATED(3),
    RELEASED(4);

    @EnumValue
    @JsonValue
    private final Integer value;

    DocStatus(Integer value) {
        this.value = value;
    }
}
