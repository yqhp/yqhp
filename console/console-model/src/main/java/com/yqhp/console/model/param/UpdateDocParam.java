package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.Doc;
import com.yqhp.console.repository.enums.DocStatus;
import com.yqhp.console.repository.enums.DocType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author jiangyitao
 */
@Data
public class UpdateDocParam implements InputConverter<Doc> {
    @NotNull(message = "类型不能为空")
    private DocType type;

    @NotBlank(message = "名称不能为空")
    @Size(max = 128, message = "名称长度不能超过{max}")
    private String name;

    @Size(max = 256, message = "描述长度不能超过{max}")
    private String description;

    private String content;

    @NotNull(message = "status不能为空")
    private DocStatus status;

    private Integer flags;
}
