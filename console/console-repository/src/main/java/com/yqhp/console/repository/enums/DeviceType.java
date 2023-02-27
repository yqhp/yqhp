package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DeviceType implements BaseEnum<Integer> {

    REAL(1),
    EMULATOR(2);

    @EnumValue
    @JsonValue
    private final Integer value;

    DeviceType(Integer value) {
        this.value = value;
    }
}
