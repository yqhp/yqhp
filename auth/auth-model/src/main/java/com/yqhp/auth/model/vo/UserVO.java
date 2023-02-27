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
