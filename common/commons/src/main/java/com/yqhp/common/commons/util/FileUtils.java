package com.yqhp.common.commons.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

/**
 * @author jiangyitao
 */
@Slf4j
public class FileUtils {

    public static String md5(File file) throws IOException {
        Validate.isTrue(file != null && file.exists());
        return DigestUtils.md5Hex(new FileInputStream(file));
    }

    public static File downloadAsTempFile(String url) throws IOException {
        return downloadAsTempFile(url, null);
    }

    public static File downloadAsTempFile(String url, String suffix) throws IOException {
        if (StringUtils.isBlank(suffix)) {
            suffix = FilenameUtils.getSuffix(url);
        }
        File tempFile = Files.createTempFile(null, suffix).toFile();
        download(url, tempFile);
        return tempFile;
    }

    public static void download(String url, File destination) throws IOException {
        Validate.notBlank(url);
        Validate.notNull(destination);
        org.apache.commons.io.FileUtils.copyURLToFile(new URL(url), destination);
    }
}
