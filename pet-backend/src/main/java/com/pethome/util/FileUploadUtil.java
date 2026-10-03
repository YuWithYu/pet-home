package com.pethome.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 文件上传工具类
 * 统一处理所有文件上传逻辑
 */
@Component
public class FileUploadUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUploadUtil.class);

    /**
     * 允许的文件扩展名白名单（小写，不含点）
     * 安全修复：原实现仅按 contentType 前缀判断，会放行 application/x-sh、application/xml 等危险类型。
     * 改为白名单 + 扩展名双重校验。
     */
    private static final Set<String> ALLOWED_IMAGE_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    ));
    private static final Set<String> ALLOWED_VIDEO_EXT = new HashSet<>(Arrays.asList(
            "mp4", "mov", "avi", "mkv", "webm"
    ));
    private static final Set<String> ALLOWED_DOC_EXT = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip"
    ));

    /**
     * 图片大小上限（10MB）：配合全局 multipart 200MB 上限形成分级限制，
     * 防止超大图片打满磁盘/带宽。
     */
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;

    @Value("${upload.path:./upload/}")
    private String uploadPath;

    @Value("${upload.url:http://localhost:8080/upload/}")
    private String uploadUrl;

    @Autowired(required = false)
    private ImageProcessUtil imageProcessUtil;

    @PostConstruct
    public void init() {
        // 规范化为绝对路径：MultipartFile.transferTo() 对相对路径会按 Servlet 容器临时目录解析
        // （Tomcat 下实际写入 %TEMP%\tomcat.xxx 而非 JVM 工作目录），导致 FileNotFoundException，
        // 且与静态资源映射（按工作目录解析）错位。统一转绝对路径后写入与访问目录一致。
        String normalized = new File(this.uploadPath).getAbsoluteFile().getAbsolutePath().replace("\\", "/");
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }
        this.uploadPath = normalized;
        File uploadDir = new File(this.uploadPath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            log.error("上传根目录创建失败: {}", this.uploadPath);
        }
        log.info("上传文件根目录（绝对路径）: {}", this.uploadPath);

        if (imageProcessUtil == null) {
            log.warn("【图片压缩未启用】ImageProcessUtil 未注入，上传图片将不压缩，请检查依赖与配置");
        } else {
            log.info("【图片上传】ImageProcessUtil 已注入，上传图片将进行压缩");
        }
    }

    @Autowired(required = false)
    private VideoTranscodeUtil videoTranscodeUtil;

    /**
     * 校验文件类型与扩展名，返回规范化后的扩展名（不含点，小写）
     * @throws IllegalArgumentException 当类型/扩展名不在白名单时
     */
    private String validateAndGetExtension(MultipartFile file, boolean allowVideo, boolean allowDoc) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("文件类型未知");
        }
        // 分级大小限制：图片不超过 10MB（视频/文档走全局 multipart 上限）
        if (contentType.startsWith("image/") && file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("图片大小不能超过10MB");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename).toLowerCase();
        if (ext.startsWith(".")) ext = ext.substring(1);
        if (ext.isEmpty()) {
            throw new IllegalArgumentException("文件扩展名缺失");
        }

        boolean allowed = false;
        if (contentType.startsWith("image/") && ALLOWED_IMAGE_EXT.contains(ext)) {
            allowed = true;
        } else if (allowVideo && contentType.startsWith("video/") && ALLOWED_VIDEO_EXT.contains(ext)) {
            allowed = true;
        } else if (allowDoc && contentType.startsWith("application/") && ALLOWED_DOC_EXT.contains(ext)) {
            allowed = true;
        }
        if (!allowed) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType + " / ." + ext);
        }
        return ext;
    }

    /**
     * 上传文件
     * @param file 上传的文件
     * @param subDir 子目录（如：background, banner 等）
     * @return 返回访问URL
     * @throws IOException 文件操作异常
     */
    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 白名单校验：图片 + 视频 + 文档
        String ext = validateAndGetExtension(file, true, true);
        String contentType = file.getContentType();

        // 生成文件名
        String filename = generateFilename(subDir, ext);

        // 创建目标目录 - 使用绝对路径
        String targetDir = uploadPath + subDir + "/";
        File dir = new File(targetDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + targetDir);
            }
        }

        // 保存文件
        File targetFile = new File(dir, filename);
        file.transferTo(targetFile);

        // 先返回 URL，视频转码放到后台异步执行，避免上传接口超时
        String finalUrl = uploadUrl + subDir + "/" + filename;
        if (contentType != null && contentType.startsWith("video/") && videoTranscodeUtil != null) {
            File dirFinal = dir;
            String filenameFinal = filename;
            CompletableFuture.runAsync(() -> {
                try {
                    String tempTranscodeName = filenameFinal.substring(0, filenameFinal.lastIndexOf('.')) + "_transcode_temp.mp4";
                    File tempTranscodeFile = new File(dirFinal, tempTranscodeName);
                    File transcodedFile = videoTranscodeUtil.transcodeVideo(targetFile, tempTranscodeFile);
                    if (transcodedFile != null && transcodedFile.exists() && transcodedFile.length() > 0) {
                        if (targetFile.delete()) {
                            if (!transcodedFile.renameTo(targetFile)) {
                                if (tempTranscodeFile.exists()) tempTranscodeFile.delete();
                            }
                        } else {
                            transcodedFile.delete();
                        }
                    } else {
                        if (tempTranscodeFile.exists()) tempTranscodeFile.delete();
                    }
                } catch (Exception e) {
                    log.error("视频后台转码异常，保留原视频: {}", e.getMessage(), e);
                }
            });
        }

        log.info("文件上传成功: subDir={}, filename={}", subDir, filename);
        return finalUrl;
    }

    /**
     * 上传图片文件（专门用于图片上传）
     * @param file 上传的图片文件
     * @param subDir 子目录
     * @return 返回访问URL（原始图片URL）
     * @throws IOException 文件操作异常
     */
    public String uploadImage(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }

        // 仅允许图片白名单
        String ext = validateAndGetExtension(file, false, false);

        // 生成文件名
        String filename = generateFilename(subDir, ext);

        // 创建目标目录
        String targetDir = uploadPath + subDir + "/";
        File dir = new File(targetDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + targetDir);
            }
        }

        // 保存原始文件
        File targetFile = new File(dir, filename);
        file.transferTo(targetFile);

        // 如果启用了图片处理，进行压缩和生成缩略图
        if (imageProcessUtil != null) {
            try {
                ImageProcessUtil.ImageProcessResult result = imageProcessUtil.processImage(targetFile, subDir, filename);
                // 返回处理后的原始图片URL（已压缩）
                return result.getOriginalUrl();
            } catch (Exception e) {
                log.error("图片压缩失败，将使用原图: file={}, subDir={}", targetFile.getAbsolutePath(), subDir, e);
            }
        } else {
            log.warn("图片未压缩（ImageProcessUtil 未注入）: {}", filename);
        }

        // 返回访问URL
        return uploadUrl + subDir + "/" + filename;
    }

    /**
     * 上传图片并返回处理结果（包含原始图和缩略图URL）
     * @param file 上传的图片文件
     * @param subDir 子目录
     * @return 图片处理结果
     * @throws IOException 文件操作异常
     */
    public ImageProcessUtil.ImageProcessResult uploadImageWithThumbnail(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }

        String ext = validateAndGetExtension(file, false, false);

        // 生成文件名
        String filename = generateFilename(subDir, ext);

        // 创建目标目录
        String targetDir = uploadPath + subDir + "/";
        File dir = new File(targetDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存原始文件
        File targetFile = new File(dir, filename);
        file.transferTo(targetFile);

        // 处理图片
        if (imageProcessUtil != null) {
            return imageProcessUtil.processImage(targetFile, subDir, filename);
        } else {
            // 如果没有图片处理工具，返回原始URL
            ImageProcessUtil.ImageProcessResult result = new ImageProcessUtil.ImageProcessResult();
            result.setOriginalUrl(uploadUrl + subDir + "/" + filename);
            return result;
        }
    }

    /**
     * 获取文件扩展名（含点，原始大小写）
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // 默认扩展名
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 生成唯一文件名
     */
    private String generateFilename(String subDir, String ext) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        if (!ext.startsWith(".")) ext = "." + ext;
        return subDir + "-" + timestamp + "-" + uuid + ext;
    }

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(uploadUrl)) {
            return false;
        }

        // 提取文件路径
        String relativePath = fileUrl.substring(uploadUrl.length());
        // 安全修复：防止路径穿越攻击（如 relativePath 含 ../.. 越界删除）
        if (relativePath.contains("..") || relativePath.contains("\\")) {
            log.warn("检测到路径穿越尝试，已拒绝删除: {}", relativePath);
            return false;
        }
        String filePath = uploadPath + relativePath;

        try {
            File file = new File(filePath);
            String canonicalUpload = new File(uploadPath).getCanonicalPath();
            String canonicalFile = file.getCanonicalPath();
            if (!canonicalFile.startsWith(canonicalUpload)) {
                log.warn("删除路径越界，已拒绝: {}", canonicalFile);
                return false;
            }
            if (file.exists()) {
                return file.delete();
            }
        } catch (IOException e) {
            log.error("删除文件时发生异常: {}", fileUrl, e);
        }
        return false;
    }

    /**
     * 获取上传路径
     */
    public String getUploadPath() {
        return uploadPath;
    }

    /**
     * 获取访问URL前缀
     */
    public String getUploadUrl() {
        return uploadUrl;
    }
}

