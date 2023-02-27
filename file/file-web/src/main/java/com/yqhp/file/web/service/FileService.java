package com.yqhp.file.web.service;

import com.yqhp.file.model.OSSFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jiangyitao
 */
public interface FileService {
    OSSFile uploadFile(MultipartFile file, boolean isTmpFile);

    String getFileUrl(String key);

    String toDurable(String key);

    void deleteFile(String key);
}
