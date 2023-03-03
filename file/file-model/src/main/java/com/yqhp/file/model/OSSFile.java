package com.yqhp.file.model;

import lombok.Data;

/**
 * @author jiangyitao
 */
@Data
public class OSSFile {
    private String url;
    private String key;
    private String name;
    private long size; // bytes
}

