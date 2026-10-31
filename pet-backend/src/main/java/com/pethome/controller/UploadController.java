package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.util.FileUploadUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "文件上传管理")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Value("${upload.path:./upload/}")
    private String uploadPath;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @PostMapping("/upload")
    @Operation(summary = "上传文件（图片会走压缩，与轮播图等一致）")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false, defaultValue = "general") String type) {
        try {
            String contentType = file.getContentType();
            boolean isImage = contentType != null && contentType.startsWith("image/");
            String subDir = isImage && ("product".equals(type) || "banner".equals(type)) ? type : "files";
            String url = isImage
                    ? fileUploadUtil.uploadImage(file, subDir)
                    : fileUploadUtil.uploadFile(file, type);
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败，请稍后重试");
        }
    }
}

