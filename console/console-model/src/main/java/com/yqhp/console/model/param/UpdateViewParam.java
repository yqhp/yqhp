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

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.View;
import com.yqhp.console.repository.enums.ViewType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class UpdateViewParam implements InputConverter<View> {
    @NotBlank(message = "docId不能为空")
    private String docId;
    @Size(max = 128, message = "deviceId长度不能超过{max}")
    private String deviceId;
    @NotNull(message = "type不能为空")
    private ViewType type;
    private String source;
    @Size(max = 1024, message = "imgUrl长度不能超过{max}")
    private String imgUrl;
    private Integer height;
    private Integer width;
}
