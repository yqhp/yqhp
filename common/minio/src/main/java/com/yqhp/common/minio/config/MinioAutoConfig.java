package com.yqhp.common.minio.config;

import com.yqhp.common.minio.MinioTemplate;
import com.yqhp.common.minio.enums.PolicyType;
import com.yqhp.common.minio.exception.MinioException;
import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
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
        try {
            return new MinioClient(
                    minioProperties.getUrl(),
                    minioProperties.getAccessKey(),
                    minioProperties.getSecretKey()
            );
        } catch (InvalidEndpointException | InvalidPortException e) {
            throw new MinioException(e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioTemplate minioTemplate(MinioClient minioClient) {
        MinioTemplate template = new MinioTemplate(minioClient, minioProperties);
        if (StringUtils.hasText(minioProperties.getBucket())) {
            template.createBucketIfAbsent(minioProperties.getBucket(), PolicyType.READ_WRITE);
        }
        return template;
    }
}
