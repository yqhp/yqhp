package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum ScreenOrientation implements BaseEnum<Integer> {

    UNKNOWN(-1),
    PORTRAIT(1), // 竖屏
    LANDSCAPE(2); // 横屏

    @EnumValue
    @JsonValue
    private final Integer value;

    ScreenOrientation(Integer value) {
        this.value = value;
    }
}
