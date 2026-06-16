package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.util.FileUploadUtil;
import com.pethome.util.VideoTranscodeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 通用文件上传控制器
 * 提供统一的文件上传接口
 */
@RestController
@RequestMapping("/api")
@Api(tags = "通用文件上传")
public class CommonUploadController {

    /** 通用图片/文档单文件上限（与 Spring multipart 总上限区分，防止异常大请求） */
    private static final long MAX_GENERIC_UPLOAD_BYTES = 25L * 1024 * 1024;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired(required = false)
    private VideoTranscodeUtil videoTranscodeUtil;

    /**
     * 视频物理存储目录与访问域名
     * 生产环境在 application-prod.yml 中配置：
     *   video.upload-path: /data/videos
     *   video.base-url: https://video.situationship.icu
     * 开发环境未配置时使用默认值，仍然走原来的 upload 目录和域名
     */
    @Value("${video.upload-path:${upload.path:./upload/}}")
    private String videoUploadPath;

    @Value("${video.base-url:${upload.url:http://localhost:8080/upload/}}")
    private String videoBaseUrl;

    private Result<String> rejectIfOversized(MultipartFile file) {
        if (file != null && file.getSize() > MAX_GENERIC_UPLOAD_BYTES) {
            return Result.error("单文件大小不能超过 25MB");
        }
        return null;
    }

    @PostMapping("/upload")
    @ApiOperation("通用文件上传接口")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            Result<String> sizeCheck = rejectIfOversized(file);
            if (sizeCheck != null) {
                return sizeCheck;
            }

            // 检查文件类型（支持图片、视频和文档）
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/") && !contentType.startsWith("application/"))) {
                return Result.error("不支持的文件类型，只支持图片、视频和文档文件");
            }

            // 使用通用目录上传
            String imageUrl = fileUploadUtil.uploadFile(file, "files");
            
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/image")
    @ApiOperation("图片上传接口")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Result<String> sizeCheck = rejectIfOversized(file);
            if (sizeCheck != null) {
                return sizeCheck;
            }
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/banner")
    @ApiOperation("轮播图上传接口")
    public Result<String> uploadBanner(@RequestParam("file") MultipartFile file) {
        try {
            Result<String> sizeCheck = rejectIfOversized(file);
            if (sizeCheck != null) {
                return sizeCheck;
            }
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("轮播图上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/product")
    @ApiOperation("商品图片上传接口")
    public Result<String> uploadProduct(@RequestParam("file") MultipartFile file) {
        try {
            Result<String> sizeCheck = rejectIfOversized(file);
            if (sizeCheck != null) {
                return sizeCheck;
            }
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("商品图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/video")
    @ApiOperation("视频上传接口")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return Result.error("只能上传视频文件");
            }

            // 生成文件名（与分片合并保持一致的命名规则）
            String originalFilename = file.getOriginalFilename();
            String ext = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".mp4";
            String finalName = FILES_SUBDIR + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;

            // 目标目录：videoUploadPath/files
            File filesDir = new File(videoUploadPath, FILES_SUBDIR);
            if (!filesDir.exists() && !filesDir.mkdirs()) {
                return Result.error("无法创建视频存储目录");
            }
            File targetFile = new File(filesDir, finalName);
            file.transferTo(targetFile);

            // 与通用上传一致：先返回 URL，后台异步转码（H.264 + faststart），转码完成后替换原文件，同一 URL 再播更快
            if (videoTranscodeUtil != null) {
                File dir = filesDir;
                String name = finalName;
                CompletableFuture.runAsync(() -> {
                    try {
                        String tempName = name.substring(0, name.lastIndexOf('.')) + "_transcode_temp.mp4";
                        File tempFile = new File(dir, tempName);
                        File transcoded = videoTranscodeUtil.transcodeVideo(targetFile, tempFile);
                        if (transcoded != null && transcoded.exists() && transcoded.length() > 0) {
                            if (targetFile.delete()) {
                                transcoded.renameTo(targetFile);
                            } else {
                                transcoded.delete();
                            }
                        } else if (tempFile.exists()) {
                            tempFile.delete();
                        }
                    } catch (Exception e) {
                        System.err.println("视频上传后转码异常，保留原视频: " + e.getMessage());
                    }
                });
            }

            String videoUrl = videoBaseUrl.endsWith("/")
                    ? videoBaseUrl + FILES_SUBDIR + "/" + finalName
                    : videoBaseUrl + "/" + FILES_SUBDIR + "/" + finalName;
            return Result.success(videoUrl);
        } catch (Exception e) {
            return Result.error("视频上传失败: " + e.getMessage());
        }
    }

    /** 分块大小建议 ≤8MB，避免超过微信单次上传 10MB 限制 */
    private static final String CHUNKS_SUBDIR = "chunks";
    private static final String FILES_SUBDIR = "files";

    @PostMapping("/upload/video/chunk")
    @ApiOperation("视频分块上传（单块）")
    public Result<Void> uploadVideoChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("totalChunks") Integer totalChunks,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("分块文件不能为空");
            }
            if (uploadId == null || uploadId.trim().isEmpty()) {
                return Result.error("uploadId 不能为空");
            }
            if (chunkIndex == null || totalChunks == null || chunkIndex < 0 || totalChunks < 1 || chunkIndex >= totalChunks) {
                return Result.error("分块参数无效");
            }
            // 分块临时目录放在视频根目录下，避免与图片混用
            String baseDir = videoUploadPath;
            File chunkDir = new File(baseDir, CHUNKS_SUBDIR + File.separator + uploadId.trim());
            if (!chunkDir.exists()) {
                if (!chunkDir.mkdirs()) {
                    return Result.error("无法创建分块目录");
                }
            }
            File chunkFile = new File(chunkDir, String.valueOf(chunkIndex));
            file.transferTo(chunkFile);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("分块上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/video/merge")
    @ApiOperation("合并视频分块")
    public Result<String> mergeVideoChunks(
            @RequestParam(value = "uploadId") String uploadId,
            @RequestParam(value = "totalChunks") Integer totalChunks,
            @RequestParam(value = "filename", required = false) String filename) {
        try {
            if (uploadId == null || uploadId.trim().isEmpty() || totalChunks == null || totalChunks < 1) {
                return Result.error("参数无效");
            }
            // 分片与合并都使用视频根目录
            String baseDir = videoUploadPath;
            File chunkDir = new File(baseDir, CHUNKS_SUBDIR + File.separator + uploadId.trim());
            if (!chunkDir.exists() || !chunkDir.isDirectory()) {
                return Result.error("分块目录不存在");
            }
            String ext = (filename != null && filename.contains(".")) ? filename.substring(filename.lastIndexOf(".")) : ".mp4";
            String finalName = FILES_SUBDIR + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
            File filesDir = new File(baseDir, FILES_SUBDIR);
            if (!filesDir.exists()) {
                filesDir.mkdirs();
            }
            File outFile = new File(filesDir, finalName);
            try (OutputStream out = new FileOutputStream(outFile)) {
                for (int i = 0; i < totalChunks; i++) {
                    File chunk = new File(chunkDir, String.valueOf(i));
                    if (!chunk.exists()) {
                        return Result.error("缺少分块: " + i);
                    }
                    Files.copy(chunk.toPath(), out);
                }
            }
            for (int i = 0; i < totalChunks; i++) {
                File chunk = new File(chunkDir, String.valueOf(i));
                if (chunk.exists()) chunk.delete();
            }
            if (chunkDir.exists()) chunkDir.delete();

            // 分片合并后同样异步转码，转码完成后替换原文件，播放更流畅
            if (videoTranscodeUtil != null) {
                File dir = filesDir;
                String name = finalName;
                File targetFile = outFile;
                CompletableFuture.runAsync(() -> {
                    try {
                        String tempName = name.substring(0, name.lastIndexOf('.')) + "_transcode_temp.mp4";
                        File tempFile = new File(dir, tempName);
                        File transcoded = videoTranscodeUtil.transcodeVideo(targetFile, tempFile);
                        if (transcoded != null && transcoded.exists() && transcoded.length() > 0) {
                            if (targetFile.delete()) {
                                transcoded.renameTo(targetFile);
                            } else {
                                transcoded.delete();
                            }
                        } else if (tempFile.exists()) {
                            tempFile.delete();
                        }
                    } catch (Exception e) {
                        System.err.println("视频合并后转码异常，保留原视频: " + e.getMessage());
                    }
                });
            }

            String videoUrl = videoBaseUrl.endsWith("/")
                    ? videoBaseUrl + FILES_SUBDIR + "/" + finalName
                    : videoBaseUrl + "/" + FILES_SUBDIR + "/" + finalName;
            return Result.success(videoUrl);
        } catch (Exception e) {
            return Result.error("合并失败: " + e.getMessage());
        }
    }
}
