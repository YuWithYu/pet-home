package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.concurrent.TranscodeGate;
import com.pethome.config.AsyncConfig;
import com.pethome.util.FileUploadUtil;
import com.pethome.util.VideoTranscodeUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 通用文件上传控制器
 * 提供统一的文件上传接口
 */
@RestController
@RequestMapping("/api")
@Tag(name = "通用文件上传")
public class CommonUploadController {

    private static final Logger log = LoggerFactory.getLogger(CommonUploadController.class);

    /** 通用图片/文档单文件上限（与 Spring multipart 总上限区分，防止异常大请求） */
    private static final long MAX_GENERIC_UPLOAD_BYTES = 25L * 1024 * 1024;

    /** uploadId 仅允许字母/数字/下划线/连字符（防止拼接路径造成目录穿越） */
    private static final java.util.regex.Pattern UPLOAD_ID_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    /** 分片总数上限（防 DoS：合并时按 totalChunks 循环，无上限可被恶意撑爆） */
    private static final int MAX_TOTAL_CHUNKS = 1000;

    /** 视频扩展名白名单（与 FileUploadUtil 的视频白名单保持一致） */
    private static final java.util.Set<String> ALLOWED_VIDEO_EXT = new java.util.HashSet<>(java.util.Arrays.asList(
            "mp4", "mov", "avi", "mkv", "webm"
    ));

    /** 校验 uploadId：防路径穿越（如 ../../）与非法字符 */
    private boolean isValidUploadId(String uploadId) {
        return uploadId != null && UPLOAD_ID_PATTERN.matcher(uploadId).matches();
    }

    @Autowired(required = false)
    private com.pethome.security.UserTokenResolver userTokenResolver;

    /**
     * 分片目录的归属标识（u=用户 / a=管理员）。
     * 安全修复：原实现分片目录只用 uploadId，任意登录用户猜中 uploadId 即可合并或删除他人分片。
     */
    private String resolveChunkOwner(HttpServletRequest request, String authHeader) {
        if (request != null) {
            Object userId = request.getAttribute("userId");
            if (userId != null) {
                return "u" + userId;
            }
            Object adminId = request.getAttribute("adminId");
            if (adminId != null) {
                return "a" + adminId;
            }
        }
        Long resolved = userTokenResolver != null ? userTokenResolver.getUserIdFromBearer(authHeader) : null;
        return resolved != null ? "u" + resolved : null;
    }

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired(required = false)
    private VideoTranscodeUtil videoTranscodeUtil;

    /** 媒体专用线程池（与业务池隔离） */
    @Autowired
    @Qualifier(AsyncConfig.MEDIA_EXECUTOR)
    private ThreadPoolTaskExecutor mediaExecutor;

    /** 转码并发闸门（Semaphore/AQS 限流） */
    @Autowired
    private TranscodeGate transcodeGate;

    /**
     * 提交后台转码：线程池排队 + 信号量限流，拿不到许可则放弃转码保留原视频。
     */
    private void submitTranscode(File dir, String name, File targetFile) {
        mediaExecutor.execute(() -> {
            if (!transcodeGate.tryEnter(5, TimeUnit.SECONDS)) {
                log.warn("转码并发已达上限，跳过转码保留原视频: {}", name);
                return;
            }
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
                log.error("视频上传后转码异常，保留原视频", e);
            } finally {
                transcodeGate.exit();
            }
        });
    }

    /**
     * 视频物理存储目录与访问域名
     * 如需自定义视频存储目录/域名，在配置中指定（项目默认全部走本机 upload 目录）：
     *   video.upload-path: /data/videos
     *   video.base-url: https://your-domain.com
     * 未配置时使用默认值，仍然走原来的 upload 目录和域名
     */
    @Value("${video.upload-path:${upload.path:./upload/}}")
    private String videoUploadPath;

    @Value("${video.base-url:${upload.url:http://localhost:8080/upload/}}")
    private String videoBaseUrl;

    /**
     * 视频根目录规范化为绝对路径：transferTo() 对相对路径会按 Servlet 容器临时目录解析
     * （Tomcat 下写入 %TEMP%\tomcat.xxx），与 mkdirs()（按工作目录解析）错位导致上传失败。
     */
    @javax.annotation.PostConstruct
    public void initVideoUploadPath() {
        String normalized = new java.io.File(videoUploadPath).getAbsoluteFile().getAbsolutePath();
        String withSep = normalized.endsWith("/") || normalized.endsWith("\\") ? normalized : normalized + "/";
        this.videoUploadPath = withSep;
    }

    private Result<String> rejectIfOversized(MultipartFile file) {
        if (file != null && file.getSize() > MAX_GENERIC_UPLOAD_BYTES) {
            return Result.error("单文件大小不能超过 25MB");
        }
        return null;
    }

    @PostMapping("/upload")
    @Operation(summary = "通用文件上传接口")
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
            log.error("文件上传失败", e);
            return Result.error("文件上传失败，请稍后重试");
        }
    }

    @PostMapping("/upload/image")
    @Operation(summary = "图片上传接口")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Result<String> sizeCheck = rejectIfOversized(file);
            if (sizeCheck != null) {
                return sizeCheck;
            }
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            return Result.success(imageUrl);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败，请稍后重试");
        }
    }

    @PostMapping("/upload/banner")
    @Operation(summary = "轮播图上传接口")
    public Result<String> uploadBanner(@RequestParam("file") MultipartFile file) {
        try {
            Result<String> sizeCheck = rejectIfOversized(file);
            if (sizeCheck != null) {
                return sizeCheck;
            }
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            return Result.success(imageUrl);
        } catch (Exception e) {
            log.error("轮播图上传失败", e);
            return Result.error("轮播图上传失败，请稍后重试");
        }
    }

    @PostMapping("/upload/product")
    @Operation(summary = "商品图片上传接口")
    public Result<String> uploadProduct(@RequestParam("file") MultipartFile file) {
        try {
            Result<String> sizeCheck = rejectIfOversized(file);
            if (sizeCheck != null) {
                return sizeCheck;
            }
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            return Result.success(imageUrl);
        } catch (Exception e) {
            log.error("商品图片上传失败", e);
            return Result.error("商品图片上传失败，请稍后重试");
        }
    }

    @PostMapping("/upload/video")
    @Operation(summary = "视频上传接口")
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
            // 安全修复：扩展名白名单校验，防止伪造 Content-Type 上传任意后缀文件
            String originalFilename = file.getOriginalFilename();
            String ext = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                    : ".mp4";
            String extNoDot = ext.startsWith(".") ? ext.substring(1) : ext;
            if (!ALLOWED_VIDEO_EXT.contains(extNoDot)) {
                return Result.error("不支持的视频格式，仅支持: " + String.join("/", ALLOWED_VIDEO_EXT));
            }
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
                submitTranscode(filesDir, finalName, targetFile);
            }

            String videoUrl = videoBaseUrl.endsWith("/")
                    ? videoBaseUrl + FILES_SUBDIR + "/" + finalName
                    : videoBaseUrl + "/" + FILES_SUBDIR + "/" + finalName;
            return Result.success(videoUrl);
        } catch (Exception e) {
            log.error("视频上传失败", e);
            return Result.error("视频上传失败，请稍后重试");
        }
    }

    /** 分块大小建议 ≤8MB，避免超过微信单次上传 10MB 限制 */
    private static final String CHUNKS_SUBDIR = "chunks";
    private static final String FILES_SUBDIR = "files";

    @PostMapping("/upload/video/chunk")
    @Operation(summary = "视频分块上传（单块）")
    public Result<Void> uploadVideoChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("totalChunks") Integer totalChunks,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (file.isEmpty()) {
                return Result.error("分块文件不能为空");
            }
            // 安全修复：uploadId 格式白名单，防止拼接路径造成目录穿越（如 ../../）
            if (!isValidUploadId(uploadId == null ? null : uploadId.trim())) {
                return Result.error("uploadId 非法");
            }
            if (file.getSize() > MAX_GENERIC_UPLOAD_BYTES) {
                return Result.error("单个分块大小不能超过 25MB");
            }
            // totalChunks 上限：防止合并阶段被恶意大数值拖死（DoS）
            if (chunkIndex == null || totalChunks == null || chunkIndex < 0 || totalChunks < 1
                    || totalChunks > MAX_TOTAL_CHUNKS || chunkIndex >= totalChunks) {
                return Result.error("分块参数无效");
            }
            // 分块临时目录放在视频根目录下，避免与图片混用
            String owner = resolveChunkOwner(request, authHeader);
            if (owner == null) {
                return Result.error(401, "请先登录后再上传");
            }
            String baseDir = videoUploadPath;
            File chunkDir = new File(baseDir, CHUNKS_SUBDIR + File.separator + owner + "-" + uploadId.trim());
            if (!chunkDir.exists()) {
                if (!chunkDir.mkdirs()) {
                    return Result.error("无法创建分块目录");
                }
            }
            File chunkFile = new File(chunkDir, String.valueOf(chunkIndex));
            file.transferTo(chunkFile);
            return Result.success(null);
        } catch (Exception e) {
            log.error("分块上传失败", e);
            return Result.error("分块上传失败，请稍后重试");
        }
    }

    @PostMapping("/upload/video/merge")
    @Operation(summary = "合并视频分块")
    public Result<String> mergeVideoChunks(
            @RequestParam(value = "uploadId") String uploadId,
            @RequestParam(value = "totalChunks") Integer totalChunks,
            @RequestParam(value = "filename", required = false) String filename,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 安全修复：uploadId 格式白名单 + totalChunks 上限（防路径穿越与合并 DoS）
            if (!isValidUploadId(uploadId == null ? null : uploadId.trim())) {
                return Result.error("uploadId 非法");
            }
            if (totalChunks == null || totalChunks < 1 || totalChunks > MAX_TOTAL_CHUNKS) {
                return Result.error("参数无效");
            }
            // 分片与合并都使用视频根目录
            String owner = resolveChunkOwner(request, authHeader);
            if (owner == null) {
                return Result.error(401, "请先登录后再上传");
            }
            String baseDir = videoUploadPath;
            File chunkDir = new File(baseDir, CHUNKS_SUBDIR + File.separator + owner + "-" + uploadId.trim());
            if (!chunkDir.exists() || !chunkDir.isDirectory()) {
                return Result.error("分块目录不存在");
            }
            // 扩展名白名单校验（与 uploadVideo 一致）
            String ext = (filename != null && filename.contains("."))
                    ? filename.substring(filename.lastIndexOf(".")).toLowerCase() : ".mp4";
            String extNoDot = ext.startsWith(".") ? ext.substring(1) : ext;
            if (!ALLOWED_VIDEO_EXT.contains(extNoDot)) {
                return Result.error("不支持的视频格式，仅支持: " + String.join("/", ALLOWED_VIDEO_EXT));
            }
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
                submitTranscode(filesDir, finalName, outFile);
            }

            String videoUrl = videoBaseUrl.endsWith("/")
                    ? videoBaseUrl + FILES_SUBDIR + "/" + finalName
                    : videoBaseUrl + "/" + FILES_SUBDIR + "/" + finalName;
            return Result.success(videoUrl);
        } catch (Exception e) {
            log.error("视频分块合并失败", e);
            return Result.error("合并失败，请稍后重试");
        }
    }
}
