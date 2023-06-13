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
package com.yqhp.file.web.service.impl;

import com.yqhp.common.commons.util.UUIDUtils;
import com.yqhp.common.minio.MinioTemplate;
import com.yqhp.file.model.OSSFile;
import com.yqhp.file.web.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String TMP_DIR = "tmp";

    @Autowired
    private MinioTemplate minioTemplate;

    @Override
    public OSSFile uploadFile(MultipartFile file, boolean isTmpFile) {
        String filename = file.getOriginalFilename();
        String fileKey = fileKey(filename, isTmpFile);
        log.info("[upload file] {} -> {}", filename, fileKey);
        minioTemplate.uploadMultipartFile(file, fileKey);

        OSSFile ossFile = new OSSFile();
        ossFile.setKey(fileKey);
        ossFile.setName(filename);
        ossFile.setSize(file.getSize());
        ossFile.setUrl(getFileUrl(fileKey));
        return ossFile;
    }

    @Override
    public String getFileUrl(String key) {
        return minioTemplate.getFileUrl(key);
    }

    @Override
    public String toDurable(String key) {
        Assert.isTrue(isTmpFile(key), key + " is not tmpFile");

        String durableFileKey = LocalDate.now().format(YYYYMMDD_FORMATTER)
                + key.substring(TMP_DIR.length());
        if (!minioTemplate.exists(durableFileKey)) {
            log.info("[copy file] {} -> {}", key, durableFileKey);
            minioTemplate.copyFile(key, durableFileKey);
        }

        return durableFileKey;
    }

    @Override
    public void deleteFile(String key) {
        if (StringUtils.hasText(key)) {
            log.info("[delete file] {}", key);
            minioTemplate.deleteFile(key);
        }
    }

    private String fileKey(String filename, boolean isTmpFile) {
        String prefix = isTmpFile
                ? TMP_DIR
                : LocalDate.now().format(YYYYMMDD_FORMATTER);
        return new StringJoiner("/")
                .add(prefix)
                .add(UUIDUtils.getUUID())
                .add(filename)
                .toString();
    }

    private boolean isTmpFile(String fileKey) {
        return fileKey.startsWith(TMP_DIR + "/");
    }
}
