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
package com.yqhp.common.commons.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author jiangyitao
 */
@Slf4j
public class FileUtils {

    public static String md5(File file) throws IOException {
        Validate.isTrue(file != null && file.exists());
        return DigestUtils.md5Hex(new FileInputStream(file));
    }

    public static File downloadIfAbsent(String url) throws IOException {
        return downloadIfAbsent(url, (File) null);
    }

    public static File downloadIfAbsent(String url, String filename) throws IOException {
        return downloadIfAbsent(url, filename, null);
    }

    public static File downloadIfAbsent(String url, File dir) throws IOException {
        String filename = org.apache.commons.io.FilenameUtils.getName(url);
        return downloadIfAbsent(url, filename, dir);
    }

    /**
     * 可以保证相同url只下载一次，该方法支持多线程调用。
     *
     * @param url       下载地址
     * @param filename  保存文件名，如"1.jpg"
     * @param directory 没有指定，默认为user.home目录
     * @return directory/md5(url)/filename
     * @throws IOException
     */
    public static File downloadIfAbsent(String url, String filename, File directory) throws IOException {
        Validate.notBlank(url);
        Validate.notBlank(filename);
        synchronized (url.intern()) {
            if (directory == null) {
                directory = org.apache.commons.io.FileUtils.getUserDirectory();
            }
            String urlMd5 = DigestUtils.md5Hex(url);
            // 文件存放路径
            File fileDir = new File(directory, urlMd5);
            if (!fileDir.exists() && !fileDir.mkdirs()) {
                throw new IOException("mkdirs err, dirs=" + fileDir);
            }
            File file = new File(fileDir, filename);
            if (!file.exists()) {
                // 先下载到临时文件再重命名，防止下载过程被中断，下次文件存在
                File tmpFile = new File(fileDir, filename + ".tmp");
                download(url, tmpFile);
                if (!tmpFile.renameTo(file)) {
                    throw new IOException(tmpFile + " renameTo " + file + " fail");
                }
            }
            return file;
        }
    }

    public static File downloadAsTempFile(String url) throws IOException {
        return downloadAsTempFile(url, FilenameUtils.getSuffix(url));
    }

    /**
     * @param suffix 文件后缀 如".png"。如果suffix==null，临时文件将会以.tmp结尾
     */
    public static File downloadAsTempFile(String url, String suffix) throws IOException {
        File tempFile = Files.createTempFile(null, suffix).toFile();
        download(url, tempFile);
        return tempFile;
    }

    public static void download(String url, File destination) throws IOException {
        Validate.notBlank(url);
        Validate.notNull(destination);
        org.apache.commons.io.FileUtils.copyURLToFile(new URL(url), destination);
    }

    public static void main(String[] args) throws IOException {
        Path tempFile1 = Files.createTempFile(null, null);
        // macos /var/folders/sn/s091fqbx74x0dmyzw_56xm4m0000gn/T/8961125342442158702.tmp
        System.out.println(tempFile1);

        Path tempFile2 = Files.createTempFile(null, "");
        // macos /var/folders/sn/s091fqbx74x0dmyzw_56xm4m0000gn/T/14798922296806345988
        System.out.println(tempFile2);

        Path tempFile3 = Files.createTempFile("hello", ".world");
        // macos /var/folders/sn/s091fqbx74x0dmyzw_56xm4m0000gn/T/hello17239928149365114006.world
        System.out.println(tempFile3);
    }
}
