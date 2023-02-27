package com.yqhp.auth.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum UserStatus implements BaseEnum<Integer> {

    DISABLED(0),
    ENABLED(1);

    /**
     * EnumValue: mybatis-plus枚举与数据库映射
     * JsonValue: 序列化输出
     */
    @EnumValue
    @JsonValue
    private final Integer value;

    UserStatus(Integer value) {
        this.value = value;
    }
}
