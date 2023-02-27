package com.yqhp.console.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class CreateViewParam extends UpdateViewParam {
    @NotBlank(message = "文档不能为空")
    private String docId;
}
