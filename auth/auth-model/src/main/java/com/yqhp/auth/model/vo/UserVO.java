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
package com.yqhp.auth.model.vo;

import com.yqhp.auth.repository.entity.User;
import com.yqhp.auth.repository.enums.UserStatus;
import com.yqhp.common.web.model.OutputConverter;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author jiangyitao
 */
@Data
public class UserVO implements OutputConverter<UserVO, User> {
    private String id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private UserStatus status;
    private LocalDateTime createTime;
}
