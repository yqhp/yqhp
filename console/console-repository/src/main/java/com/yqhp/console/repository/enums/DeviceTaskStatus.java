package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DeviceTaskStatus implements BaseEnum<Integer> {

    TODO(0),
    RECEIVED(1),
    STARTED(2),
    SUCCESSFUL(3),
    FAILED(4),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    DeviceTaskStatus(Integer value) {
        this.value = value;
    }
}
