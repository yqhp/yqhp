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
