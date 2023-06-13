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
