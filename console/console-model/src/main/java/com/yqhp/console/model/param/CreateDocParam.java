package com.yqhp.console.model.param;

import com.yqhp.console.repository.enums.DocType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author jiangyitao
 */
@Data
public class CreateDocParam extends UpdateDocParam {
    @NotBlank(message = "项目不能为空")
    private String projectId;
    @NotNull(message = "类型不能为空")
    private DocType type;
}
