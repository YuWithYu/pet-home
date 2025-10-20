package com.pethome.controller;

import com.pethome.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "文件上传管理")
public class UploadController {

    @Value("${upload.path:./upload/}")
    private String uploadPath;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false, defaultValue = "") String type) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 根据类型确定子目录
            String subDir = "";
            if (type != null && !type.isEmpty()) {
                subDir = type + "/";
            }

            // 创建上传目录（包括子目录）
            String fullUploadPath = uploadPath + subDir;
            File uploadDir = new File(fullUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成文件名：UUID + 原始扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(fullUploadPath, fileName);
            Files.copy(file.getInputStream(), filePath);

            // 返回相对路径（包含子目录）: product/文件名.jpg
            return Result.success(subDir + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}

