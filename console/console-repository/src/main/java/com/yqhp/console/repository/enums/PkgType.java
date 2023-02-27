package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum PkgType implements BaseEnum<Integer> {

    DOC(1),
    ACTION(2),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    PkgType(Integer value) {
        this.value = value;
    }
}
