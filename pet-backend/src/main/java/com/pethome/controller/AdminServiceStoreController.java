package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.ServiceStore;
import com.pethome.service.ServiceStoreService;
import com.pethome.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 管理后台-服务门店列表（用于门店客服等，带数据隔离）
 */
@RestController
@RequestMapping("/api/admin/service-stores")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
@Api(tags = "管理后台-服务门店")
public class AdminServiceStoreController {

    @Autowired
    private ServiceStoreService serviceStoreService;

    @Autowired(required = false)
    private AdminContext adminContext;

    @GetMapping
    @ApiOperation("获取服务门店列表（管理员用，带数据隔离）")
    public Result<List<ServiceStore>> getServiceStores(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentServiceStoreId = null;
            if (adminContext != null && token != null) {
                currentServiceStoreId = adminContext.getCurrentServiceStoreId(token);
            }

            List<ServiceStore> list;
            if (currentServiceStoreId != null) {
                ServiceStore store = serviceStoreService.getStoreById(currentServiceStoreId);
                list = store != null ? Collections.singletonList(store) : new ArrayList<>();
            } else {
                list = serviceStoreService.getAllActiveStores();
            }
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取服务门店列表失败: " + e.getMessage());
        }
    }
}
