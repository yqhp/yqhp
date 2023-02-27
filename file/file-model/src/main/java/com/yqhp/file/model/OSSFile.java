package com.yqhp.file.model;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class OSSFile {
    private String url;
    private String key;
    private String originalFilename;
    private long size; // bytes
}

