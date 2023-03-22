package com.yqhp.console.model.param;

import com.yqhp.common.web.model.InputConverter;
import com.yqhp.console.repository.entity.PluginFile;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author jiangyitao
 */
@Data
public class UpdatePluginFileParam implements InputConverter<PluginFile> {
    @NotBlank(message = "name不能为空")
    private String name;
    @NotBlank(message = "key不能为空")
    private String key;
    @NotBlank(message = "url不能为空")
    private String url;
    @NotNull(message = "size不能为空")
    private Long size;
}
