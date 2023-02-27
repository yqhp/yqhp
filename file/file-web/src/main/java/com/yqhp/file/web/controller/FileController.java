package com.yqhp.file.web.controller;

import com.yqhp.file.model.OSSFile;
import com.yqhp.file.web.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author jiangyitao
 */
@Validated
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping
    public OSSFile uploadFile(@NotNull(message = "文件不能为空") MultipartFile file, boolean isTmpFile) {
        return fileService.uploadFile(file, isTmpFile);
    }

    @GetMapping("/url")
    public String getFileUrl(@NotBlank(message = "key不能为空") String key) {
        return fileService.getFileUrl(key);
    }

    @PutMapping("/toDurable")
    public String toDurable(@NotBlank(message = "key不能为空") String key) {
        return fileService.toDurable(key);
    }

    @DeleteMapping
    public void deleteFile(@NotBlank(message = "key不能为空") String key) {
        fileService.deleteFile(key);
    }
}
