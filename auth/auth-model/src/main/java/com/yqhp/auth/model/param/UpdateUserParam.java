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
package com.yqhp.auth.model.param;

import com.yqhp.auth.repository.entity.User;
import com.yqhp.common.web.model.InputConverter;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class UpdateUserParam implements InputConverter<User> {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 128, message = "昵称长度不能超过{max}")
    private String nickname;

    @Size(max = 1024, message = "头像url长度不能超过{max}")
    private String avatar;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    @Size(max = 128, message = "邮箱长度不能超过{max}")
    private String email;
}
