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
