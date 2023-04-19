package com.yqhp.agent.web.service.impl;

import com.yqhp.agent.web.config.prop.AgentProperties;
import com.yqhp.agent.web.service.PluginService;
import com.yqhp.common.commons.util.FileUtils;
import com.yqhp.console.repository.entity.PluginFile;
import com.yqhp.console.repository.jsonfield.PluginDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class PluginServiceImpl implements PluginService {

    private final File baseDirectory;

    public PluginServiceImpl(AgentProperties agentProperties) {
        String baseDir = agentProperties.getPlugin().getBaseDir();
        log.info("plugin base dir: {}", baseDir);
        baseDirectory = new File(baseDir);
        if (!baseDirectory.exists() && !baseDirectory.mkdirs()) {
            throw new IllegalStateException("mk plugin base dir fail, dir=" + baseDir);
        }
    }

    @Override
    public List<File> downloadIfAbsent(PluginDTO plugin) throws IOException {
        if (plugin == null || CollectionUtils.isEmpty(plugin.getFiles())) {
            return new ArrayList<>();
        }

        synchronized (plugin.getId().intern()) {
            File pluginDir = new File(baseDirectory, plugin.getId());
            if (!pluginDir.exists() && !pluginDir.mkdir()) {
                throw new IOException("mk plugin dir fail, dir=" + pluginDir);
            }

            List<File> files = new ArrayList<>(plugin.getFiles().size());
            for (PluginFile fileInfo : plugin.getFiles()) {
                File file = new File(pluginDir, fileInfo.getName());
                if (!file.exists()) {
                    // 先下载到临时文件再重命名，防止下载过程被中断，下次文件存在
                    File tmpFile = new File(pluginDir, fileInfo.getName() + ".tmp");
                    log.info("[plugin][{}][{}]download from {}", plugin.getName(), fileInfo.getName(), fileInfo.getUrl());
                    FileUtils.download(fileInfo.getUrl(), tmpFile);
                    if (!tmpFile.renameTo(file)) {
                        throw new IOException(tmpFile + " renameTo " + file + " fail");
                    }
                    log.info("[plugin][{}][{}]download complete -> {}", plugin.getName(), fileInfo.getName(), file);
                }
                files.add(file);
            }
            return files;
        }
    }
}
