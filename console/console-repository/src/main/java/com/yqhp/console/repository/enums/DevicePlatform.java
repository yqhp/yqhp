package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DevicePlatform implements BaseEnum<Integer> {

    Android(1),
    iOS(2);

    @EnumValue
    @JsonValue
    private final Integer value;

    DevicePlatform(Integer value) {
        this.value = value;
    }
}
