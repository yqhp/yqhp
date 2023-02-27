package com.yqhp.console.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DeviceStatus implements BaseEnum<Integer> {

    OFFLINE(0),
    IDLE(1),
    BUSY(2);

    @JsonValue
    private final Integer value;

    DeviceStatus(int value) {
        this.value = value;
    }
}
