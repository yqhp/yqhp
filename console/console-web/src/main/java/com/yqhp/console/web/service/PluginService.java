/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

    Plugin createPlugin(CreatePluginParam param);

    Plugin updatePlugin(String id, UpdatePluginParam param);

    Plugin getPluginById(String id);

    void deleteById(String id);

    List<Plugin> listByProjectId(String projectId);

    List<PluginDTO> listDTOByProjectId(String projectId);
}
