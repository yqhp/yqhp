package com.yqhp.agent.web.service;

import com.yqhp.console.repository.jsonfield.PluginDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author jiangyitao
 */
public interface PluginService {
    List<File> downloadIfAbsent(PluginDTO plugin) throws IOException;
}
