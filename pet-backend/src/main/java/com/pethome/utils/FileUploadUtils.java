package com.pethome.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUploadUtils {

    private static final String UPLOAD_DIR = "upload/";

    public static String saveFile(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // 创建目录
        String uploadPath = UPLOAD_DIR + subDir;
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        return "/upload/" + subDir + "/" + filename;
    }

    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath.replaceFirst("^/+", ""));
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
}
