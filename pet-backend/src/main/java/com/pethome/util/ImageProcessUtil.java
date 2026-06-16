package com.pethome.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * 图片处理工具类
 * 支持图片压缩、缩略图生成、CDN URL处理
 */
@Component
public class ImageProcessUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessUtil.class);
    
    @Value("${upload.path:./upload/}")
    private String uploadPath;
    
    @Value("${upload.url:http://localhost:8080/upload/}")
    private String uploadUrl;
    
    @Value("${upload.cdn.enabled:false}")
    private boolean cdnEnabled;
    
    @Value("${upload.cdn.url:}")
    private String cdnUrl;
    
    @Value("${upload.image.compress:true}")
    private boolean compressEnabled;
    
    @Value("${upload.image.max-width:1024}")
    private int maxWidth;
    
    @Value("${upload.image.max-height:1024}")
    private int maxHeight;
    
    @Value("${upload.image.quality:0.75}")
    private float quality;
    
    @Value("${upload.image.thumbnail-enabled:true}")
    private boolean thumbnailEnabled;
    
    @Value("${upload.image.thumbnail-width:400}")
    private int thumbnailWidth;
    
    @Value("${upload.image.thumbnail-height:400}")
    private int thumbnailHeight;

    @PostConstruct
    public void init() {
        logger.info("【图片压缩配置】compress={}, maxWidth={}, maxHeight={}, quality={}, uploadPath={}",
                compressEnabled, maxWidth, maxHeight, quality, uploadPath);
    }
    
    /**
     * 处理上传的图片：压缩并生成缩略图
     * @param originalFile 原始图片文件
     * @param subDir 子目录（如：images, banner等）
     * @param filename 文件名
     * @return 图片处理结果对象，包含原始图和缩略图URL
     */
    public ImageProcessResult processImage(File originalFile, String subDir, String filename) {
        ImageProcessResult result = new ImageProcessResult();
        
        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(originalFile);
            if (originalImage == null) {
                throw new IOException("无法读取图片文件");
            }
            
            // 1. 压缩主图（缩放+质量）；PNG 转 JPEG 以大幅减小体积，与用户端上传体验一致
            String writtenFilename = filename;
            if (compressEnabled) {
                writtenFilename = compressImage(originalFile, subDir, filename, originalImage);
            }
            result.setOriginalUrl(buildImageUrl(subDir, writtenFilename));
            
            // 2. 生成缩略图（如果需要）
            if (thumbnailEnabled) {
                String thumbnailFilename = "thumb_" + filename;
                File thumbnailFile = generateThumbnail(originalImage, subDir, thumbnailFilename);
                if (thumbnailFile != null) {
                    String thumbnailUrl = buildImageUrl(subDir, thumbnailFilename);
                    result.setThumbnailUrl(thumbnailUrl);
                }
            }
            
            logger.info("图片处理完成: originalUrl={}, thumbnailUrl={}", 
                    result.getOriginalUrl(), result.getThumbnailUrl());
            
            return result;
            
        } catch (Exception e) {
            logger.error("图片处理失败: file={}, error={}", originalFile.getName(), e.getMessage(), e);
            // 处理失败时返回原始文件URL
            result.setOriginalUrl(buildImageUrl(subDir, filename));
            return result;
        }
    }
    
    /**
     * 压缩图片并覆盖原文件；PNG/GIF 转为 JPEG 以大幅减小体积（管理员上传与用户端一样快）
     * @return 实际写入的文件名（PNG 转 JPEG 时为 xxx.jpg）
     */
    private String compressImage(File originalFile, String subDir, String filename, BufferedImage originalImage)
            throws IOException {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        int newWidth = width;
        int newHeight = height;
        if (width > maxWidth || height > maxHeight) {
            double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
            newWidth = Math.max(1, (int) (width * scale));
            newHeight = Math.max(1, (int) (height * scale));
        }

        BufferedImage outImage = originalImage;
        if (newWidth != width || newHeight != height) {
            outImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = outImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();
            logger.info("图片缩放: {}x{} -> {}x{}", width, height, newWidth, newHeight);
        }

        String format = getImageFormat(filename);
        File dir = getUploadDir(subDir);
        File targetFile = new File(dir, filename);

        // PNG/GIF 无有效有损压缩，转为 JPEG 后体积可降为几百 KB，与用户端上传体验一致
        boolean saveAsJpeg = "png".equalsIgnoreCase(format) || "gif".equalsIgnoreCase(format);
        if (saveAsJpeg) {
            int lastDot = filename.lastIndexOf('.');
            String baseName = lastDot > 0 ? filename.substring(0, lastDot) : filename;
            String jpegFilename = baseName + ".jpg";
            File jpegFile = new File(dir, jpegFilename);
            writeJpegWithQuality(outImage, jpegFile, quality);
            if (targetFile.exists() && !targetFile.equals(jpegFile)) {
                targetFile.delete();
            }
            return jpegFilename;
        }
        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            writeJpegWithQuality(outImage, targetFile, quality);
        } else {
            ImageIO.write(outImage, format, targetFile);
        }
        return filename;
    }

    private void writeJpegWithQuality(BufferedImage image, File file, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(image, "jpg", file);
            return;
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(Math.max(0.1f, Math.min(1f, quality)));
        }
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }
    
    /**
     * 生成缩略图
     */
    private File generateThumbnail(BufferedImage originalImage, String subDir, String thumbnailFilename) 
            throws IOException {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // 计算缩放比例，保持宽高比
        double scale = Math.min((double) thumbnailWidth / width, (double) thumbnailHeight / height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        
        // 创建缩略图
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumbnail.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        // 保存缩略图
        File thumbnailFile = new File(getUploadDir(subDir), thumbnailFilename);
        String format = getImageFormat(thumbnailFilename);
        ImageIO.write(thumbnail, format, thumbnailFile);
        
        logger.info("缩略图生成完成: {}x{} -> {}x{}", width, height, newWidth, newHeight);
        return thumbnailFile;
    }
    
    /**
     * 构建图片URL（支持CDN）
     */
    private String buildImageUrl(String subDir, String filename) {
        String baseUrl = cdnEnabled && !cdnUrl.isEmpty() ? cdnUrl : uploadUrl;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + subDir + "/" + filename;
    }
    
    /**
     * 获取上传目录
     */
    private File getUploadDir(String subDir) {
        String dirPath = uploadPath;
        if (!dirPath.endsWith("/") && !dirPath.endsWith("\\")) {
            dirPath += "/";
        }
        dirPath += subDir;
        
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    
    /**
     * 获取图片格式
     */
    private String getImageFormat(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return "png";
        } else if (lower.endsWith(".gif")) {
            return "gif";
        } else {
            return "jpg";
        }
    }
    
    /**
     * 图片处理结果类
     */
    public static class ImageProcessResult {
        private String originalUrl;
        private String thumbnailUrl;
        
        public String getOriginalUrl() {
            return originalUrl;
        }
        
        public void setOriginalUrl(String originalUrl) {
            this.originalUrl = originalUrl;
        }
        
        public String getThumbnailUrl() {
            return thumbnailUrl;
        }
        
        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }
}

