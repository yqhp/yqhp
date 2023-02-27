package com.yqhp.common.minio;

import com.yqhp.common.minio.config.MinioProperties;
import com.yqhp.common.minio.enums.PolicyType;
import com.yqhp.common.minio.exception.MinioException;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @author jiangyitao
 */
public class MinioTemplate {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioTemplate(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public void createBucketIfAbsent(String bucket, PolicyType policyType) {
        try {
            if (!minioClient.bucketExists(bucket)) {
                minioClient.makeBucket(bucket);
                minioClient.setBucketPolicy(bucket, getPolicy(bucket, policyType));
            }
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public void uploadFile(File file, String fileKey) {
        try {
            uploadObj(minioProperties.getBucket(), new FileInputStream(file), null, fileKey);
        } catch (FileNotFoundException e) {
            throw new MinioException(e);
        }
    }

    public void uploadMultipartFile(MultipartFile multipartFile, String fileKey) {
        try {
            uploadObj(minioProperties.getBucket(), multipartFile.getInputStream(), multipartFile.getContentType(), fileKey);
        } catch (IOException e) {
            throw new MinioException(e);
        }
    }

    public void uploadObj(String bucket, InputStream inputStream, String contentType, String fileKey) {
        try {
            minioClient.putObject(bucket, fileKey, inputStream, (long) inputStream.available(), null, null, contentType);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public void deleteFile(String fileKey) {
        deleteFile(minioProperties.getBucket(), fileKey);
    }

    public void deleteFile(String bucket, String fileKey) {
        try {
            minioClient.removeObject(bucket, fileKey);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public void deleteFiles(Iterable<String> fileKeys) {
        deleteFiles(minioProperties.getBucket(), fileKeys);
    }

    public void deleteFiles(String bucket, Iterable<String> fileKeys) {
        try {
            minioClient.removeObjects(bucket, fileKeys);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public boolean exists(String fileKey) {
        return exists(minioProperties.getBucket(), fileKey);
    }

    public boolean exists(String bucket, String fileKey) {
        try {
            return statFile(bucket, fileKey) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void copyFile(String fromFileKey, String toFileKey) {
        copyFile(minioProperties.getBucket(), fromFileKey, minioProperties.getBucket(), toFileKey);
    }

    public void copyFile(String fromBucket, String fromFileKey, String toBucket, String toFileKey) {
        try {
            minioClient.copyObject(fromBucket, fromFileKey, toBucket, toFileKey);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public String getFileUrl(String fileKey) {
        return getFileUrl(minioProperties.getBucket(), fileKey);
    }

    public String getFileUrl(String bucket, String fileKey) {
        try {
            return minioClient.getObjectUrl(bucket, fileKey);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public void getFile(String fileKey, String filePath) {
        getFile(minioProperties.getBucket(), fileKey, filePath);
    }

    public void getFile(String bucket, String fileKey, String filePath) {
        try {
            minioClient.getObject(bucket, fileKey, filePath);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public ObjectStat statFile(String fileKey) {
        return statFile(minioProperties.getBucket(), fileKey);
    }

    public ObjectStat statFile(String bucket, String fileKey) {
        try {
            return minioClient.statObject(bucket, fileKey);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public Iterable<Result<Item>> listFiles(String prefix) {
        return listFiles(minioProperties.getBucket(), prefix);
    }

    public Iterable<Result<Item>> listFiles(String bucket, String prefix) {
        try {
            return minioClient.listObjects(bucket, prefix);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    private String getPolicy(String bucket, PolicyType policyType) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    \"Statement\": [\n");
        sb.append("        {\n");
        sb.append("            \"Action\": [\n");

        switch (policyType) {
            case WRITE:
                sb.append("                \"s3:GetBucketLocation\",\n");
                sb.append("                \"s3:ListBucketMultipartUploads\"\n");
                break;
            case READ_WRITE:
                sb.append("                \"s3:GetBucketLocation\",\n");
                sb.append("                \"s3:ListBucket\",\n");
                sb.append("                \"s3:ListBucketMultipartUploads\"\n");
                break;
            default:
                sb.append("                \"s3:GetBucketLocation\"\n");
                break;
        }

        sb.append("            ],\n");
        sb.append("            \"Effect\": \"Allow\",\n");
        sb.append("            \"Principal\": \"*\",\n");
        sb.append("            \"Resource\": \"arn:aws:s3:::");
        sb.append(bucket);
        sb.append("\"\n");
        sb.append("        },\n");
        if (PolicyType.READ.equals(policyType)) {
            sb.append("        {\n");
            sb.append("            \"Action\": [\n");
            sb.append("                \"s3:ListBucket\"\n");
            sb.append("            ],\n");
            sb.append("            \"Effect\": \"Deny\",\n");
            sb.append("            \"Principal\": \"*\",\n");
            sb.append("            \"Resource\": \"arn:aws:s3:::");
            sb.append(bucket);
            sb.append("\"\n");
            sb.append("        },\n");
        }
        sb.append("        {\n");
        sb.append("            \"Action\": ");

        switch (policyType) {
            case WRITE:
                sb.append("[\n");
                sb.append("                \"s3:AbortMultipartUpload\",\n");
                sb.append("                \"s3:DeleteObject\",\n");
                sb.append("                \"s3:ListMultipartUploadParts\",\n");
                sb.append("                \"s3:PutObject\"\n");
                sb.append("            ],\n");
                break;
            case READ_WRITE:
                sb.append("[\n");
                sb.append("                \"s3:AbortMultipartUpload\",\n");
                sb.append("                \"s3:DeleteObject\",\n");
                sb.append("                \"s3:GetObject\",\n");
                sb.append("                \"s3:ListMultipartUploadParts\",\n");
                sb.append("                \"s3:PutObject\"\n");
                sb.append("            ],\n");
                break;
            default:
                sb.append("\"s3:GetObject\",\n");
                break;
        }

        sb.append("            \"Effect\": \"Allow\",\n");
        sb.append("            \"Principal\": \"*\",\n");
        sb.append("            \"Resource\": \"arn:aws:s3:::");
        sb.append(bucket);
        sb.append("/*\"\n");
        sb.append("        }\n");
        sb.append("    ],\n");
        sb.append("    \"Version\": \"2012-10-17\"\n");
        sb.append("}\n");
        return sb.toString();
    }
}
