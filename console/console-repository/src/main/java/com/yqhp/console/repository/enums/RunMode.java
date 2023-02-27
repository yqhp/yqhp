package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum RunMode implements BaseEnum<Integer> {

    EFFICIENT(1), // 高效模式，平均分配action
    COMPATIBLE(2), // 兼容模式，执行同一份action
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    RunMode(Integer value) {
        this.value = value;
    }
}
