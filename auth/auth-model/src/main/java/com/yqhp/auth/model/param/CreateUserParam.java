package com.yqhp.auth.model.param;

import com.yqhp.auth.repository.enums.UserStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class CreateUserParam extends UpdateUserParam {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 128, message = "用户名长度不能超过{max}")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 5, max = 100, message = "密码长度必须在{min}-{max}之间")
    private String password;

    private UserStatus status;
}
