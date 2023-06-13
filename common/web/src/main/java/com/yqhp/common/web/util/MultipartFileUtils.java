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
package com.yqhp.common.web.util;

import com.yqhp.common.commons.util.FilenameUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.nio.file.Files;

/**
 * @author jiangyitao
 */
public class MultipartFileUtils {

    public static MultipartFile toMultipartFile(File file) throws IOException {
        return toMultipartFile(file, "file");
    }

    public static MultipartFile toMultipartFile(File file, String fieldName) throws IOException {
        return toMultipartFile(file, fieldName, false);
    }

    public static MultipartFile toMultipartFile(File file, String fieldName, boolean isFormField) throws IOException {
        DiskFileItem fileItem = new DiskFileItem(fieldName, Files.probeContentType(file.toPath()),
                isFormField, file.getName(), (int) file.length(), file.getParentFile());
        fileItem.setDefaultCharset("UTF-8");
        try (InputStream in = new FileInputStream(file);
             OutputStream out = fileItem.getOutputStream()) {
            IOUtils.copy(in, out);
        }
        return new CommonsMultipartFile(fileItem);
    }

    public static File toTempFile(MultipartFile multipartFile) throws IOException {
        String suffix = FilenameUtils.getSuffix(multipartFile.getOriginalFilename());
        File tempFile = Files.createTempFile(null, suffix).toFile();
        org.apache.commons.io.FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), tempFile);
        return tempFile;
    }
}
