package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.Project;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Map;

/**
 * @author jiangyitao
 */
@Data
public class UpdateProjectParam implements InputConverter<Project> {
    @NotBlank(message = "项目名不能为空")
    @Size(max = 128, message = "项目名长度不能超过{max}")
    private String name;

    @Size(max = 256, message = "描述长度不能超过{max}")
    private String description;

    private Map<String, Object> extra;
}
