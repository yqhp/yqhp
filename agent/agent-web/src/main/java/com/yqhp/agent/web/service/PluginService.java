package com.yqhp.agent.web.service;

import com.yqhp.console.repository.entity.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface PluginService {
    List<File> getFiles(Plugin plugin) throws IOException;
}
