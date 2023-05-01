package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePluginFileParam;
import com.yqhp.console.repository.entity.PluginFile;

import java.util.Collection;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface PluginFileService extends IService<PluginFile> {
    void createPluginFile(CreatePluginFileParam param);

    PluginFile getPluginFileById(String id);

    void deleteById(String id);

    List<PluginFile> listByPluginId(String pluginId);

    List<PluginFile> listInPluginIds(Collection<String> pluginIds);
}
