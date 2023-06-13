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
package com.yqhp.file.rpc;

import com.yqhp.file.model.OSSFile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jiangyitao
 */
@FeignClient(name = "file-service", path = "/oss/file", contextId = "file")
public interface FileRpc {
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    OSSFile uploadFile(@RequestPart(value = "file") MultipartFile file, @RequestPart(value = "isTmpFile") boolean isTmpFile);

    @GetMapping("/url")
    String getFileUrl(@RequestParam("key") String key);

    @PutMapping("/toDurable")
    String toDurable(@RequestParam("tmpFileKey") String tmpFileKey);

    @DeleteMapping
    void deleteFile(@RequestParam("key") String key);
}
