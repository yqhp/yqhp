package com.yqhp.common.minio.config;

import com.yqhp.common.minio.MinioTemplate;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author jiangyitao
 */
@Configuration
@ConditionalOnClass(MinioTemplate.class)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfig {

    @Autowired
    private MinioProperties minioProperties;

    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioTemplate minioTemplate(MinioClient minioClient) {
        MinioTemplate template = new MinioTemplate(minioClient, minioProperties);
        if (StringUtils.hasText(minioProperties.getBucket())) {
            template.createBucketIfAbsent(minioProperties.getBucket());
        }
        return template;
    }
}
