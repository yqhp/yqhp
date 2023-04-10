package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DocKind implements BaseEnum<Integer> {

    JSH_INIT(1),
    JSH_ACTION(2),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    DocKind(Integer value) {
        this.value = value;
    }
}
