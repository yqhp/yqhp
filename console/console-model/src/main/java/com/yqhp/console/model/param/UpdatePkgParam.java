package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.Pkg;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class UpdatePkgParam implements InputConverter<Pkg> {

    private String parentId;

    @NotBlank(message = "包名不能为空")
    @Size(max = 128, message = "包名长度不能超过{max}")
    private String name;

    @Size(max = 256, message = "描述长度不能超过{max}")
    private String description;

    private Integer flags;

}
