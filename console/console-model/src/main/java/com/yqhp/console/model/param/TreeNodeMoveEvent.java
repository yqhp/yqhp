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
package com.yqhp.console.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author jiangyitao
 */
@Data
public class TreeNodeMoveEvent {
    @NotBlank(message = "from不能为空")
    private String from;
    @NotBlank(message = "to不能为空")
    private String to;
    @NotNull(message = "type不能为空")
    private Type type;

    public boolean isInner() {
        return Type.INNER.equals(type);
    }

    public boolean isBefore() {
        return Type.BEFORE.equals(type);
    }

    public boolean isAfter() {
        return Type.AFTER.equals(type);
    }

    enum Type {
        BEFORE,
        AFTER,
        INNER
    }
}
