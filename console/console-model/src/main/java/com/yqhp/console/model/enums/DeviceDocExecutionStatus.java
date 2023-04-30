package com.yqhp.console.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DeviceDocExecutionStatus implements BaseEnum<Integer> {

    UNFINISHED(-1),
    FAILED(0),
    SUCCESS(1);

    @JsonValue
    private final Integer value;

    DeviceDocExecutionStatus(int value) {
        this.value = value;
    }
}
