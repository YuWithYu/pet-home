package com.pethome.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 视频转码工具类
 * 使用FFmpeg将视频转换为H.264编码（小程序兼容）
 */
@Component
public class VideoTranscodeUtil {

    private static final Logger logger = LoggerFactory.getLogger(VideoTranscodeUtil.class);

    @Value("${upload.video.transcode.enabled:true}")
    private boolean transcodeEnabled;

    @Value("${upload.video.transcode.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    @Value("${upload.video.transcode.codec:libx264}")
    private String videoCodec;

    @Value("${upload.video.transcode.audio-codec:aac}")
    private String audioCodec;

    @Value("${upload.video.transcode.preset:medium}")
    private String preset;

    @Value("${upload.video.transcode.crf:26}")
    private int crf;

    /** 默认 720p，便于小程序/网页流畅播放，降低卡顿 */
    @Value("${upload.video.transcode.max-width:1280}")
    private int maxWidth;

    @Value("${upload.video.transcode.max-height:720}")
    private int maxHeight;

    @Value("${upload.video.transcode.max-duration:300}")
    private int maxDuration;

    @Value("${upload.video.transcode.timeout:300}")
    private int timeoutSeconds;

    /**
     * 启动时检查FFmpeg是否可用
     */
    @PostConstruct
    public void init() {
        if (transcodeEnabled) {
            logger.info("==========================================");
            logger.info("🎬 视频转码功能已启用");
            logger.info("FFmpeg路径: {}", ffmpegPath);
            if (isFFmpegAvailable()) {
                logger.info("✅ FFmpeg可用，视频将自动转码为H.264");
            } else {
                logger.warn("⚠️ FFmpeg不可用，视频转码将跳过");
                logger.warn("💡 请安装FFmpeg或设置正确的ffmpeg-path");
            }
            logger.info("==========================================");
        } else {
            logger.info("视频转码功能已禁用");
        }
    }

    /**
     * 检查FFmpeg是否可用
     */
    public boolean isFFmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ffmpegPath, "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (finished && process.exitValue() == 0) {
                logger.info("✅ FFmpeg可用: {}", ffmpegPath);
                return true;
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("⚠️ FFmpeg不可用: {} - {}", ffmpegPath, e.getMessage());
        }
        return false;
    }

    /**
     * 转码视频为H.264编码
     * @param inputFile 输入视频文件
     * @param outputFile 输出视频文件（如果为null，则自动生成）
     * @return 转码后的文件路径，如果转码失败返回null
     */
    public File transcodeVideo(File inputFile, File outputFile) {
        if (!transcodeEnabled) {
            logger.info("视频转码已禁用，跳过转码: {}", inputFile.getName());
            return null;
        }

        if (!isFFmpegAvailable()) {
            logger.warn("FFmpeg不可用，跳过转码: {}", inputFile.getName());
            return null;
        }

        try {
            // 如果未指定输出文件，自动生成
            if (outputFile == null) {
                String inputPath = inputFile.getAbsolutePath();
                String baseName = inputPath.substring(0, inputPath.lastIndexOf('.'));
                String extension = ".mp4";
                outputFile = new File(baseName + "_h264" + extension);
            }

            // 构建FFmpeg命令
            List<String> command = buildFFmpegCommand(inputFile, outputFile);

            logger.info("开始转码视频: {} -> {}", inputFile.getName(), outputFile.getName());
            logger.debug("FFmpeg命令: {}", String.join(" ", command));

            // 执行转码
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 等待转码完成（带超时）
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                logger.error("视频转码超时: {}", inputFile.getName());
                return null;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.error("视频转码失败，退出码: {} - {}", exitCode, inputFile.getName());
                return null;
            }

            // 检查输出文件是否存在且有效
            if (outputFile.exists() && outputFile.length() > 0) {
                logger.info("✅ 视频转码成功: {} ({} -> {})", 
                    inputFile.getName(), 
                    formatFileSize(inputFile.length()),
                    formatFileSize(outputFile.length()));
                return outputFile;
            } else {
                logger.error("转码后的文件不存在或为空: {}", outputFile.getAbsolutePath());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            logger.error("视频转码异常: {} - {}", inputFile.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建FFmpeg转码命令
     */
    private List<String> buildFFmpegCommand(File inputFile, File outputFile) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-i");
        command.add(inputFile.getAbsolutePath());
        
        // 视频编码参数
        command.add("-c:v");
        command.add(videoCodec);
        command.add("-preset");
        command.add(preset);
        command.add("-crf");
        command.add(String.valueOf(crf));
        
        // 分辨率限制（如果需要）
        if (maxWidth > 0 || maxHeight > 0) {
            String scaleFilter = buildScaleFilter();
            if (scaleFilter != null) {
                command.add("-vf");
                command.add(scaleFilter);
            }
        }
        
        // 限制码率，避免高码率导致小程序/弱网卡顿（约 1.5Mbps 适合 720p 流畅）
        command.add("-maxrate");
        command.add("1500k");
        command.add("-bufsize");
        command.add("3000k");
        
        // 音频编码参数
        command.add("-c:a");
        command.add(audioCodec);
        command.add("-b:a");
        command.add("128k"); // 音频码率
        
        // 时长限制（如果需要）
        if (maxDuration > 0) {
            command.add("-t");
            command.add(String.valueOf(maxDuration));
        }
        
        // 其他参数
        command.add("-movflags");
        command.add("+faststart"); // 优化网络播放
        // 清除旋转元数据：转码时 FFmpeg 已将元数据应用到像素，输出 rotate=0 避免播放器二次旋转导致横屏变竖屏
        command.add("-metadata:s:v:0");
        command.add("rotate=0");
        command.add("-y"); // 覆盖输出文件
        
        command.add(outputFile.getAbsolutePath());
        
        return command;
    }

    /**
     * 构建分辨率缩放滤镜
     * 保持宽高比，限制最大宽度和高度
     */
    private String buildScaleFilter() {
        if (maxWidth > 0 && maxHeight > 0) {
            // 保持宽高比，限制最大尺寸（使用简单的scale语法）
            return String.format("scale=w='min(%d,iw)':h='min(%d,ih)':force_original_aspect_ratio=decrease", maxWidth, maxHeight);
        } else if (maxWidth > 0) {
            return String.format("scale=%d:-1", maxWidth);
        } else if (maxHeight > 0) {
            return String.format("scale=-1:%d", maxHeight);
        }
        return null;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * 检查视频是否需要转码（简单检查：如果已经是MP4且可能是H.264，可能不需要转码）
     * 注意：这个方法只是简单判断，实际编码格式需要FFmpeg检测
     */
    public boolean needsTranscode(File videoFile) {
        String fileName = videoFile.getName().toLowerCase();
        // 如果已经是.mp4，可能已经是H.264，但为了确保兼容性，仍然转码
        // 其他格式（.mov, .avi, .mkv等）肯定需要转码
        return !fileName.endsWith(".mp4") || transcodeEnabled;
    }
}
