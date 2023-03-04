package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DocType implements BaseEnum<Integer> {

    JSH_DEFINE(1),
    JSH_RUN(2),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    DocType(Integer value) {
        this.value = value;
    }
}
