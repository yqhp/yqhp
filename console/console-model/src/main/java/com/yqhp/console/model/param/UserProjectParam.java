package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.UserProject;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class UserProjectParam implements InputConverter<UserProject> {
    @NotBlank(message = "用户不能为空")
    private String userId;
    @NotBlank(message = "项目不能为空")
    private String projectId;
}
