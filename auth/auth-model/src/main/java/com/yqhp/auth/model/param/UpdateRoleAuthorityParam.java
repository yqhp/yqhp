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

import com.yqhp.auth.repository.entity.RoleAuthority;
import com.yqhp.common.web.model.InputConverter;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class UpdateRoleAuthorityParam implements InputConverter<RoleAuthority> {
    @NotBlank(message = "权限名不能为空")
    @Size(max = 32, message = "权限名长度不能超过{max}")
    private String authorityName;

    @NotBlank(message = "权限值不能为空")
    @Size(max = 32, message = "权限值长度不能超过{max}")
    private String authorityValue;
}
