package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum ViewType implements BaseEnum<Integer> {

    ANDROID_NATIVE(1),
    ANDROID_WEB(2),
    iOS_NATIVE(3),
    iOS_WEB(4);

    @EnumValue
    @JsonValue
    private final Integer value;

    ViewType(Integer value) {
        this.value = value;
    }
}
