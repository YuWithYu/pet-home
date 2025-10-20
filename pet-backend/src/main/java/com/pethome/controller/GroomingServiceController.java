package com.pethome.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.GroomingService;
import com.pethome.service.GroomingServiceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/grooming-services")
@Api(tags = "洗护服务管理")
@Validated
public class GroomingServiceController {

    @Autowired
    private GroomingServiceService groomingServiceService;

    @GetMapping("/page")
    @ApiOperation("分页查询洗护服务")
    public Result<IPage<GroomingService>> getGroomingServicePage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("服务分类") @RequestParam(required = false) String category,
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("服务名称") @RequestParam(required = false) String name) {

        Page<GroomingService> page = new Page<>(pageNo, pageSize);
        IPage<GroomingService> result = groomingServiceService.getGroomingServicePage(page, category, status, name);
        return Result.success(result);
    }

    @GetMapping
    @ApiOperation("获取所有洗护服务")
    public Result<List<GroomingService>> getAllGroomingServices() {
        List<GroomingService> services = groomingServiceService.list();
        return Result.success(services);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取洗护服务")
    public Result<GroomingService> getGroomingServiceById(@PathVariable Long id) {
        GroomingService service = groomingServiceService.getById(id);
        if (service != null) {
            return Result.success(service);
        } else {
            return Result.error("服务不存在");
        }
    }

    @PostMapping
    @ApiOperation("创建洗护服务")
    public Result<GroomingService> createGroomingService(@RequestBody String requestBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);
            
            GroomingService service = new GroomingService();
            service.setName((String) requestData.get("name"));
            service.setDescription((String) requestData.get("description"));
            service.setCategory((String) requestData.get("category"));
            service.setPrice(new BigDecimal(requestData.get("price").toString()));
            service.setDuration(Integer.valueOf(requestData.get("duration").toString()));
            service.setStatus((String) requestData.get("status"));
            
            // 处理tags字段 - 如果是数组则转换为JSON字符串
            Object tagsObj = requestData.get("tags");
            if (tagsObj != null) {
                if (tagsObj instanceof java.util.List) {
                    service.setTags(objectMapper.writeValueAsString(tagsObj));
                } else {
                    service.setTags(tagsObj.toString());
                }
            }
            
            boolean success = groomingServiceService.save(service);
            if (success) {
                return Result.success(service);
            } else {
                return Result.error("创建服务失败");
            }
        } catch (Exception e) {
            return Result.error("创建服务失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新洗护服务")
    public Result<GroomingService> updateGroomingService(@PathVariable Long id, @RequestBody GroomingService service) {
        service.setId(id);
        boolean success = groomingServiceService.updateById(service);
        if (success) {
            return Result.success(service);
        } else {
            return Result.error("更新服务失败");
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除洗护服务")
    public Result<String> deleteGroomingService(@PathVariable Long id) {
        boolean success = groomingServiceService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除服务失败");
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新服务状态")
    public Result<String> updateServiceStatus(@PathVariable Long id, @RequestParam String status) {
        GroomingService service = new GroomingService();
        service.setId(id);
        service.setStatus(status);
        boolean success = groomingServiceService.updateById(service);
        if (success) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("更新状态失败");
        }
    }

    @PostMapping("/upload")
    @ApiOperation("上传服务图片")
    public Result<String> uploadServiceImage(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) {
        return updateServiceImage(id, file);
    }

    @PostMapping("/{id}/image")
    @ApiOperation("更新服务图片")
    public Result<String> updateServiceImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择要上传的图片");
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                return Result.error("只能上传图片文件");
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String filename = "grooming-service-" + id + extension;
            
            // 上传目录
            String uploadDir = "C:/Users/Yu/Desktop/pet-home/upload/grooming-service/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 保存文件
            java.io.File targetFile = new java.io.File(dir, filename);
            file.transferTo(targetFile);
            
            // 更新数据库中的图片URL
            String imageUrl = "/upload/grooming-service/" + filename;
            GroomingService service = groomingServiceService.getById(id);
            if (service != null) {
                service.setImageUrl(imageUrl);
                groomingServiceService.updateById(service);
            }
            
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}