package com.yqhp.common.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jiangyitao
 */
@Data
@ConfigurationProperties(prefix = "oss.minio")
public class MinioProperties {
    private String url;
    private String bucket;
    private String accessKey;
    private String secretKey;
}
