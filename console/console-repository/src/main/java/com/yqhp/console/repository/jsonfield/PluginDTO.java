package com.yqhp.console.repository.jsonfield;

import com.yqhp.console.repository.entity.Plugin;
import com.yqhp.console.repository.entity.PluginFile;
import lombok.Data;

import java.util.List;

/**
 * @author jiangyitao
 */
@Data
public class PluginDTO extends Plugin {
    private List<PluginFile> files;
}
