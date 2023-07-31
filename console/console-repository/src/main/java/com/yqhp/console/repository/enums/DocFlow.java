/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.console.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.yqhp.common.base.BaseEnum;
import lombok.Getter;

/**
 * @author jiangyitao
 */
@Getter
public enum DocFlow implements BaseEnum<Integer> {

    KEEP_RUNNING_NEXT_IF_ERROR(1), // 运行失败时，继续执行后续doc
    STOP_RUNNING_NEXT_IF_ERROR(2), // 运行失败时，停止执行后续doc
    ;

    @EnumValue
    @JsonValue
    private final Integer value;

    DocFlow(Integer value) {
        this.value = value;
    }
}
