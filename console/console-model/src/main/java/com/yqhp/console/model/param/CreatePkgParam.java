package com.yqhp.console.model.param;

import com.yqhp.console.repository.enums.PkgType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author jiangyitao
 */
@Data
public class CreatePkgParam extends UpdatePkgParam {
    @NotBlank(message = "项目不能为空")
    private String projectId;
    @NotNull(message = "目录类型不能为空")
    private PkgType type;

    private String parentId;
}
