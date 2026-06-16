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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 文件上传工具类
 * 统一处理所有文件上传逻辑
 */
@Component
public class FileUploadUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUploadUtil.class);

    @Value("${upload.path:./upload/}")
    private String uploadPath;

    @Value("${upload.url:http://localhost:8080/upload/}")
    private String uploadUrl;
    
    @Autowired(required = false)
    private ImageProcessUtil imageProcessUtil;

    @PostConstruct
    public void init() {
        if (imageProcessUtil == null) {
            log.warn("【图片压缩未启用】ImageProcessUtil 未注入，上传图片将不压缩，请检查依赖与配置");
        } else {
            log.info("【图片上传】ImageProcessUtil 已注入，上传图片将进行压缩");
        }
    }

    @Autowired(required = false)
    private VideoTranscodeUtil videoTranscodeUtil;

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

        // 检查文件类型（支持图片、视频和文档）
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/") && !contentType.startsWith("application/"))) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持图片、视频和文档");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = generateFilename(subDir, extension);

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
                    System.err.println("视频后台转码异常，保留原视频: " + e.getMessage());
                }
            });
        }

        // 返回访问URL
        System.out.println("========== 文件上传成功 ==========");
        System.out.println("上传目录: " + subDir);
        System.out.println("文件名: " + filename);
        System.out.println("配置的uploadUrl: " + uploadUrl);
        System.out.println("返回的文件URL: " + finalUrl);
        System.out.println("====================================");
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

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = generateFilename(subDir, extension);

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
                log.error("图片压缩失败，将使用原图: file={}, subDir={}, error={}", targetFile.getAbsolutePath(), subDir, e.getMessage(), e);
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

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = generateFilename(subDir, extension);

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
     * 获取文件扩展名
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
    private String generateFilename(String subDir, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return subDir + "-" + timestamp + "-" + uuid + extension;
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
        String filePath = uploadPath + relativePath;
        
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
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
