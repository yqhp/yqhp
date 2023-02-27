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

    JAVA(1, "java"),
    JSH(2, "jsh"),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    private final String ext;

    DocType(Integer value, String ext) {
        this.value = value;
        this.ext = ext;
    }
}
