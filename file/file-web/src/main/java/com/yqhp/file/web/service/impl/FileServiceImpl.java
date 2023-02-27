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
        log.info("[upload multipart file] {} -> {}", filename, fileKey);
        minioTemplate.uploadMultipartFile(file, fileKey);

        OSSFile ossFile = new OSSFile();
        ossFile.setKey(fileKey);
        ossFile.setOriginalFilename(filename);
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
            log.info("copyFile {} -> {}", key, durableFileKey);
            minioTemplate.copyFile(key, durableFileKey);
        }

        return durableFileKey;
    }

    @Override
    public void deleteFile(String key) {
        if (StringUtils.hasText(key)) {
            log.info("delete {}", key);
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
