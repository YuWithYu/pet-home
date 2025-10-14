package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Banner;
import com.pethome.service.BannerService;
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
@Api(tags = "横幅管理")
public class BannerController {

    @Autowired
    private BannerService bannerService;
    
    @Value("${upload.path:./upload/}")
    private String uploadPath;
    
    @Value("${server.host:http://localhost}")
    private String serverHost;
    
    @Value("${server.port:8080}")
    private String serverPort;

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
        List<Banner> banners = bannerService.getAllBanners();

        // 转换为管理员前端期望的格式
        List<Map<String, Object>> bannerList = banners.stream()
            .map(banner -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", banner.getId());
                item.put("title", banner.getTitle());
                item.put("description", banner.getDescription());
                item.put("filename", banner.getFilename());
                item.put("originalName", banner.getOriginalName());

                // 将图片路径转换为完整URL（管理员前端访问）
                String imageUrl = banner.getFileUrl();
                if (imageUrl != null && !imageUrl.startsWith("http")) {
                    if (!imageUrl.startsWith("/")) {
                        imageUrl = "/" + imageUrl;
                    }
                    imageUrl = serverHost + ":" + serverPort + imageUrl;
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

        return Result.success(bannerList);
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
        
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 获取原始文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadPath, fileName);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 创建Banner对象并保存到数据库
            Banner banner = new Banner();
            banner.setTitle(title != null ? title : originalFilename);
            banner.setDescription(description != null ? description : "");
            banner.setFilename(fileName);  // 只存储文件名
            banner.setOriginalName(originalFilename);
            banner.setFileUrl("/upload/" + fileName);  // 存储相对路径
            banner.setFileSize(file.getSize());
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
}
