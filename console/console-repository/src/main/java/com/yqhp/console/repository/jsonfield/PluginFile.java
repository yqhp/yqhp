package com.yqhp.console.repository.jsonfield;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author jiangyitao
 */
@Data
public class PluginFile {
    @NotBlank(message = "插件文件名不能为空")
    private String name;
    @NotBlank(message = "插件文件url不能为空")
    private String url;
    @NotNull(message = "插件文件大小不能为空")
    private Long size;
}
