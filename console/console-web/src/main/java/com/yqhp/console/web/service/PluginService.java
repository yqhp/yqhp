package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreatePluginParam;
import com.yqhp.console.model.param.UpdatePluginParam;
import com.yqhp.console.model.param.query.PluginPageQuery;
import com.yqhp.console.repository.entity.Plugin;
import com.yqhp.console.repository.jsonfield.PluginDTO;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface PluginService extends IService<Plugin> {
    IPage<Plugin> pageBy(PluginPageQuery query);

    Plugin createPlugin(CreatePluginParam createPluginParam);

    Plugin updatePlugin(String id, UpdatePluginParam updatePluginParam);

    Plugin getPluginById(String id);

    void deleteById(String id);

    List<Plugin> listByProjectId(String projectId);

    List<PluginDTO> listDTOByProjectId(String projectId);
}
