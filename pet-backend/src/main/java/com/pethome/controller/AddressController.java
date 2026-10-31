package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Address;
import com.pethome.service.AddressService;
import com.pethome.security.UserTokenResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/addresses")
@Tag(name = "地址管理")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserTokenResolver userTokenResolver;

    /**
     * 从请求头中获取token并解析userId
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        // 统一走 UserTokenResolver（本项目 token 解析的唯一入口），避免各 Controller 自行实现造成口径漂移
        return userTokenResolver.resolveUserId(request.getHeader("Authorization"));
    }

    @GetMapping("/list")
    @Operation(summary = "获取当前用户所有地址")
    public Result<List<Address>> getAddressList(
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        // 安全修复 #49：userId 必须取自 token，不再 fallback 到前端传入的 query 参数，
        // 避免攻击者不带 token 直接传他人 userId 查看他人地址。
        Long finalUserId = getUserIdFromToken(request);
        if (finalUserId == null) {
            log.warn("[AddressController] getAddressList - 未登录或 token 无效");
            return Result.error(401, "未登录或 token 无效");
        }
        // 兼容旧调用方仍传 userId 参数：若与 token 解析的 userId 不一致，按越权处理
        if (userId != null && !userId.equals(finalUserId)) {
            log.warn("[AddressController] getAddressList - 越权尝试：token userId={} 但请求 userId={}", finalUserId, userId);
            return Result.error(403, "无权查看他人地址");
        }
        log.debug("[AddressController] getAddressList - 查询地址列表，userId: {}", finalUserId);
        List<Address> addresses = addressService.getUserAddresses(finalUserId);
        log.debug("[AddressController] getAddressList - 查询到 {} 条地址", addresses != null ? addresses.size() : 0);
        return Result.success(addresses);
    }

    @PostMapping("/create")
    @Operation(summary = "创建地址")
    public Result<Address> createAddress(
            @RequestBody Address address,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        try {
            log.debug("[AddressController] createAddress - 接收到的地址数据: {}", address);
            // 安全修复 #49：userId 必须取自 token，body 与 query 中的 userId 仅做兼容校验
            Long finalUserId = getUserIdFromToken(request);
            if (finalUserId == null) {
                log.warn("[AddressController] createAddress - 未登录或 token 无效");
                return Result.error(401, "未登录或 token 无效");
            }
            // 兼容校验：若 body / query 显式指定了其他 userId，按越权处理
            if (address != null && address.getUserId() != null && !address.getUserId().equals(finalUserId)) {
                log.warn("[AddressController] createAddress - 越权尝试：token userId={} 但 body userId={}", finalUserId, address.getUserId());
                return Result.error(403, "无权为他人创建地址");
            }
            if (userId != null && !userId.equals(finalUserId)) {
                log.warn("[AddressController] createAddress - 越权尝试：token userId={} 但 query userId={}", finalUserId, userId);
                return Result.error(403, "无权为他人创建地址");
            }
            address.setUserId(finalUserId);
            log.debug("[AddressController] createAddress - 设置 userId: {}", finalUserId);
            Address savedAddress = addressService.createAddress(address);
            log.debug("[AddressController] createAddress - 保存成功，地址ID: {}", savedAddress.getId());
            return Result.success(savedAddress);
        } catch (Exception e) {
            log.error("[AddressController] createAddress - 异常: {}", e.getMessage(), e);
            return Result.error("创建地址失败，请稍后重试");
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新地址")
    public Result<Address> updateAddress(@RequestBody Address address, HttpServletRequest request) {
        // 安全修复 #49：原逻辑 if(userId != null) 才校验归属，token 失败时直接放行——严重越权。
        // 现强制要求登录，并校验地址归属。
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            log.warn("[AddressController] updateAddress - 未登录或 token 无效");
            return Result.error(401, "未登录或 token 无效");
        }
        if (address == null || address.getId() == null) {
            return Result.error(400, "地址ID不能为空");
        }
        Address existingAddress = addressService.getAddressById(address.getId());
        if (existingAddress == null) {
            return Result.error(404, "地址不存在");
        }
        if (!userId.equals(existingAddress.getUserId())) {
            log.warn("[AddressController] updateAddress - 越权尝试：token userId={} 但地址归属 userId={}", userId, existingAddress.getUserId());
            return Result.error(403, "无权修改此地址");
        }
        // 强制使用 token 中的 userId，忽略前端传入的 userId
        address.setUserId(userId);
        return Result.success(addressService.updateAddress(address));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址")
    public Result<Boolean> deleteAddress(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        // 安全修复 #49：userId 必须取自 token，不再 fallback 到 query 参数
        Long finalUserId = getUserIdFromToken(request);
        if (finalUserId == null) {
            log.warn("[AddressController] deleteAddress - 未登录或 token 无效");
            return Result.error(401, "未登录或 token 无效");
        }
        if (userId != null && !userId.equals(finalUserId)) {
            log.warn("[AddressController] deleteAddress - 越权尝试：token userId={} 但 query userId={}", finalUserId, userId);
            return Result.error(403, "无权删除他人地址");
        }
        Address address = addressService.getAddressById(id);
        if (address == null) {
            return Result.error(404, "地址不存在");
        }
        if (!address.getUserId().equals(finalUserId)) {
            log.warn("[AddressController] deleteAddress - 越权尝试：token userId={} 但地址归属 userId={}", finalUserId, address.getUserId());
            return Result.error(403, "无权删除此地址");
        }
        return Result.success(addressService.deleteAddress(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取地址详情")
    public Result<Address> getAddressDetail(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复 #49：原实现完全无归属校验，任何人知道 id 即可读取他人地址（含姓名/电话/详细地址）。
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            log.warn("[AddressController] getAddressDetail - 未登录或 token 无效");
            return Result.error(401, "未登录或 token 无效");
        }
        Address address = addressService.getAddressById(id);
        if (address == null) {
            return Result.error(404, "地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            log.warn("[AddressController] getAddressDetail - 越权尝试：token userId={} 但地址归属 userId={}", userId, address.getUserId());
            return Result.error(403, "无权查看此地址");
        }
        return Result.success(address);
    }

    @PutMapping("/set-default/{id}")
    @Operation(summary = "设置默认地址")
    public Result<Address> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "未登录或 token 无效");
        }
        // 安全修复 #49：设置默认前需校验该地址确实归属当前用户
        Address existing = addressService.getAddressById(id);
        if (existing == null) {
            return Result.error(404, "地址不存在");
        }
        if (!userId.equals(existing.getUserId())) {
            log.warn("[AddressController] setDefaultAddress - 越权尝试：token userId={} 但地址归属 userId={}", userId, existing.getUserId());
            return Result.error(403, "无权操作此地址");
        }
        return Result.success(addressService.setDefaultAddress(userId, id));
    }

    @GetMapping("/default")
    @Operation(summary = "获取当前用户默认地址")
    public Result<Address> getDefaultAddress(
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        // 安全修复 #49：userId 必须取自 token，不再 fallback 到 query 参数
        Long finalUserId = getUserIdFromToken(request);
        if (finalUserId == null) {
            return Result.error(401, "未登录或 token 无效");
        }
        if (userId != null && !userId.equals(finalUserId)) {
            log.warn("[AddressController] getDefaultAddress - 越权尝试：token userId={} 但 query userId={}", finalUserId, userId);
            return Result.error(403, "无权查看他人默认地址");
        }
        Address address = addressService.getDefaultAddress(finalUserId);
        return Result.success(address);
    }
}


