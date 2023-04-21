package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.PluginFile;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Data
public class UpdatePluginFileParam implements InputConverter<PluginFile> {
    @NotBlank(message = "name不能为空")
    private String name;
    @NotBlank(message = "url不能为空")
    private String url;
    private Long size;
}
