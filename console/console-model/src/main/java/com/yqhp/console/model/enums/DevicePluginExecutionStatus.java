package com.yqhp.console.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DevicePluginExecutionStatus implements BaseEnum<Integer> {

    UNFINISHED(-1),
    FAILED(0),
    SUCCESS(1);

    @JsonValue
    private final Integer value;

    DevicePluginExecutionStatus(int value) {
        this.value = value;
    }
}
