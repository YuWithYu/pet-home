package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Banner;
import com.pethome.service.BannerService;
import com.pethome.util.FileUploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/banner", "/api/banners"})
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "横幅管理")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private FileUploadUtil fileUploadUtil;
    
    @Value("${upload.path:./upload/}")
    private String uploadPath;
    
    @Value("${host:http://localhost}")
    private String serverHost;
    
    @Value("${server.port:8080}")
    private String serverPort;

    /** 生产环境公网 base URL（如 https://situationship.icu），设后接口返回的图片地址用此域名，不拼端口 */
    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @GetMapping("/page")
    @ApiOperation("分页查询横幅")
    public Result<IPage<Banner>> getBannerPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Banner> page = new Page<>(pageNo, pageSize);
        IPage<Banner> result = bannerService.getBannerList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建横幅")
    public Result<Banner> createBanner(@RequestBody Banner banner) {
        return Result.success(bannerService.createBanner(banner));
    }

    @PutMapping("/update")
    @ApiOperation("更新横幅")
    public Result<Banner> updateBanner(@RequestBody Banner banner) {
        return Result.success(bannerService.updateBanner(banner));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除横幅")
    public Result<Boolean> deleteBanner(@PathVariable Long id) {
        return Result.success(bannerService.deleteBanner(id));
    }

    @GetMapping("/list")
    @ApiOperation("获取横幅列表")
    public Result<java.util.List<Map<String, Object>>> getBannerList() {
        List<Banner> banners = bannerService.getAllBannersForManagement();

        return Result.success(buildBannerList(banners));
    }

    @GetMapping("/active")
    @ApiOperation("获取启用的横幅列表（小程序端）")
    public Result<java.util.List<Map<String, Object>>> getActiveBannerList() {
        List<Banner> allBanners = bannerService.getAllBannersForManagement();
        List<Banner> activeBanners = allBanners.stream()
            .filter(b -> "active".equals(b.getStatus()))
            .collect(Collectors.toList());
        return Result.success(buildBannerList(activeBanners));
    }

    private java.util.List<Map<String, Object>> buildBannerList(List<Banner> banners) {
        return banners.stream()
            .map(banner -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", banner.getId());
                String title = banner.getTitle();
                if (title == null || title.isEmpty() || title.contains(".jpg") || title.contains(".png") || title.contains("微信图片")) {
                    title = "宠物之家";
                }
                item.put("title", title);
                item.put("description", banner.getDescription());
                item.put("filename", banner.getFilename());
                item.put("originalName", banner.getOriginalName());

                // 将图片路径转换为完整URL（生产用公网域名，开发用 host:port）
                String imageUrl = banner.getFileUrl();
                if (imageUrl != null && !imageUrl.startsWith("http")) {
                    if (!imageUrl.startsWith("/")) {
                        imageUrl = "/" + imageUrl;
                    }
                    String base = (publicBaseUrl != null && !publicBaseUrl.trim().isEmpty())
                        ? publicBaseUrl.trim().replaceAll("/+$", "")
                        : (serverHost + ":" + serverPort);
                    imageUrl = base + imageUrl;
                }
                // 生产环境：数据库中若存的是 localhost/127.0.0.1，统一替换为公网域名
                if (imageUrl != null && publicBaseUrl != null && !publicBaseUrl.trim().isEmpty()) {
                    String base = publicBaseUrl.trim().replaceAll("/+$", "");
                    if (imageUrl.startsWith("http://localhost") || imageUrl.startsWith("https://localhost") || imageUrl.startsWith("http://127.0.0.1")) {
                        int schemeEnd = imageUrl.indexOf("://");
                        int pathStart = schemeEnd >= 0 ? imageUrl.indexOf("/", schemeEnd + 3) : -1;
                        imageUrl = (pathStart > 0) ? base + imageUrl.substring(pathStart) : base + "/upload/";
                    }
                }
                item.put("url", imageUrl);
                item.put("fileUrl", imageUrl); // 兼容小程序接口

                item.put("size", banner.getFileSize());
                item.put("fileSize", banner.getFileSize()); // 兼容小程序接口
                item.put("fileType", banner.getFileType());
                item.put("status", banner.getStatus());
                item.put("sortOrder", banner.getSortOrder());
                item.put("createTime", banner.getCreateTime());
                item.put("updateTime", banner.getUpdateTime());

                return item;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @ApiOperation("获取横幅详情")
    public Result<Banner> getBannerDetail(@PathVariable Long id) {
        return Result.success(bannerService.getBannerById(id));
    }

    @PostMapping("/upload")
    @ApiOperation("上传轮播图文件")
    public Result<Banner> uploadBanner(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description) {
        
        try {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            // 压缩后取磁盘上的实际大小，避免列表显示“假的大体积”
            long fileSize = file.getSize();
            try {
                int idx = imageUrl != null ? imageUrl.indexOf("/upload/") : -1;
                if (idx >= 0 && idx + 8 < imageUrl.length()) {
                    String rel = imageUrl.substring(idx + 8).replace("\\", "/");
                    String base = (uploadPath != null && !uploadPath.isEmpty()) ? uploadPath.replaceAll("/+$", "") + "/" : "upload/";
                    File saved = new File(base + rel);
                    if (saved.exists()) fileSize = saved.length();
                }
            } catch (Exception ignored) { }
            // 创建Banner对象并保存到数据库
            Banner banner = new Banner();
            banner.setTitle(title != null && !title.isEmpty() ? title : "宠物之家");
            banner.setDescription(description != null ? description : "");
            banner.setFilename(file.getOriginalFilename());
            banner.setOriginalName(file.getOriginalFilename());
            banner.setFileUrl(imageUrl);
            banner.setFileSize(fileSize);
            banner.setFileType(file.getContentType());
            banner.setStatus("active");
            banner.setSortOrder(0);
            banner.setCreateTime(LocalDateTime.now());
            banner.setUpdateTime(LocalDateTime.now());

            Banner savedBanner = bannerService.createBanner(banner);
            return Result.success(savedBanner);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新横幅状态")
    public Result<Banner> updateBannerStatus(@PathVariable Long id, @RequestParam String status) {
        Banner banner = bannerService.getBannerById(id);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }
        banner.setStatus(status);
        banner.setUpdateTime(LocalDateTime.now());
        return Result.success(bannerService.updateBanner(banner));
    }
}
