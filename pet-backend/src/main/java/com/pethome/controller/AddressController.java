package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.Result;
import com.pethome.entity.Address;
import com.pethome.entity.User;
import com.pethome.mapper.UserMapper;
import com.pethome.service.AddressService;
import com.pethome.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "地址管理")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 从请求头中获取token并解析userId
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            log.debug("[AddressController] getUserIdFromToken - Authorization header存在: {}", authHeader != null);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.debug("[AddressController] getUserIdFromToken - token长度: {}", token.length());

                // 直接尝试解析username（getUsernameFromToken内部会验证token）
                // 注意：token中可能存储的是username、email或phone
                String tokenSubject = jwtUtil.getUsernameFromToken(token);
                log.debug("[AddressController] getUserIdFromToken - 从token解析的subject: {}", tokenSubject);
                if (tokenSubject != null && !tokenSubject.isEmpty()) {
                    User user = null;

                    // 1. 先尝试通过username查找
                    user = userMapper.selectOne(new QueryWrapper<User>()
                            .eq("username", tokenSubject)
                            .last("LIMIT 1"));
                    if (user != null) {
                        log.debug("[AddressController] getUserIdFromToken - 通过username找到用户，userId: {}", user.getId());
                        return user.getId();
                    }

                    // 2. 如果username找不到，尝试通过email查找
                    user = userMapper.selectOne(new QueryWrapper<User>()
                            .eq("email", tokenSubject)
                            .last("LIMIT 1"));
                    if (user != null) {
                        log.debug("[AddressController] getUserIdFromToken - 通过email找到用户，userId: {}", user.getId());
                        return user.getId();
                    }

                    // 3. 如果email也找不到，尝试通过phone查找（因为登录时token可能是用phone生成的）
                    user = userMapper.selectOne(new QueryWrapper<User>()
                            .eq("phone", tokenSubject)
                            .last("LIMIT 1"));
                    if (user != null) {
                        log.debug("[AddressController] getUserIdFromToken - 通过phone找到用户，userId: {}", user.getId());
                        return user.getId();
                    }

                    // 如果都找不到
                    log.warn("[AddressController] getUserIdFromToken - 未找到用户，tokenSubject: {} (已尝试username、email、phone)", tokenSubject);
                } else {
                    log.warn("[AddressController] getUserIdFromToken - 无法从token解析subject（token可能无效或已过期）");
                }
            } else {
                log.debug("[AddressController] getUserIdFromToken - Authorization header不存在或格式不正确");
            }
        } catch (Exception e) {
            log.error("[AddressController] getUserIdFromToken - 异常: {}", e.getMessage(), e);
        }
        return null;
    }

    @GetMapping("/list")
    @ApiOperation("获取当前用户所有地址")
    public Result<List<Address>> getAddressList(
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        // 优先从token获取userId，确保安全性（用户只能查看自己的地址）
        Long finalUserId = getUserIdFromToken(request);
        if (finalUserId == null) {
            // 如果token中没有userId，再尝试使用参数中的userId（向后兼容）
            finalUserId = userId;
        }
        if (finalUserId == null) {
            log.warn("[AddressController] getAddressList - 错误: 未登录或用户ID无效");
            return Result.error("未登录或用户ID无效");
        }
        log.debug("[AddressController] getAddressList - 查询地址列表，userId: {}", finalUserId);
        List<Address> addresses = addressService.getUserAddresses(finalUserId);
        log.debug("[AddressController] getAddressList - 查询到 {} 条地址", addresses != null ? addresses.size() : 0);
        return Result.success(addresses);
    }

    @PostMapping("/create")
    @ApiOperation("创建地址")
    public Result<Address> createAddress(
            @RequestBody Address address,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        try {
            log.debug("[AddressController] createAddress - 接收到的地址数据: {}", address);
            // 优先从 token 获取 userId，其次用 body 中的 userId，最后用 query 参数 userId（兜底）
            Long finalUserId = getUserIdFromToken(request);
            if (finalUserId == null && address != null && address.getUserId() != null) {
                finalUserId = address.getUserId();
                log.debug("[AddressController] createAddress - 使用 body 中的 userId: {}", finalUserId);
            }
            if (finalUserId == null && userId != null && userId > 0) {
                finalUserId = userId;
                log.debug("[AddressController] createAddress - 使用 query 参数 userId: {}", finalUserId);
            }
            if (finalUserId == null) {
                log.warn("[AddressController] createAddress - 错误: 未登录或用户ID无效");
                return Result.error("未登录或用户ID无效");
            }
            address.setUserId(finalUserId);
            log.debug("[AddressController] createAddress - 设置 userId: {}", finalUserId);
            Address savedAddress = addressService.createAddress(address);
            log.debug("[AddressController] createAddress - 保存成功，地址ID: {}", savedAddress.getId());
            return Result.success(savedAddress);
        } catch (Exception e) {
            log.error("[AddressController] createAddress - 异常: {}", e.getMessage(), e);
            return Result.error("创建地址失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新地址")
    public Result<Address> updateAddress(@RequestBody Address address, HttpServletRequest request) {
        // 验证地址是否属于当前用户
        Long userId = getUserIdFromToken(request);
        if (userId != null) {
            Address existingAddress = addressService.getAddressById(address.getId());
            if (existingAddress == null) {
                return Result.error("地址不存在");
            }
            if (!existingAddress.getUserId().equals(userId)) {
                return Result.error("无权修改此地址");
            }
            address.setUserId(userId);
        }
        return Result.success(addressService.updateAddress(address));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除地址")
    public Result<Boolean> deleteAddress(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        Long finalUserId = getUserIdFromToken(request);
        if (finalUserId == null) {
            finalUserId = userId;
        }
        if (finalUserId == null) {
            return Result.error("未登录或用户ID无效");
        }
        Address address = addressService.getAddressById(id);
        if (address == null) {
            return Result.error("地址不存在");
        }
        if (!address.getUserId().equals(finalUserId)) {
            return Result.error("无权删除此地址");
        }
        return Result.success(addressService.deleteAddress(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取地址详情")
    public Result<Address> getAddressDetail(@PathVariable Long id) {
        Address address = addressService.getAddressById(id);
        if (address == null) {
            return Result.error("地址不存在");
        }
        return Result.success(address);
    }

    @PutMapping("/set-default/{id}")
    @ApiOperation("设置默认地址")
    public Result<Address> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return Result.error("未登录或用户ID无效");
        }
        return Result.success(addressService.setDefaultAddress(userId, id));
    }

    @GetMapping("/default")
    @ApiOperation("获取当前用户默认地址")
    public Result<Address> getDefaultAddress(
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        Long finalUserId = userId;
        if (finalUserId == null) {
            finalUserId = getUserIdFromToken(request);
        }
        if (finalUserId == null) {
            return Result.error("未登录或用户ID无效");
        }
        Address address = addressService.getDefaultAddress(finalUserId);
        return Result.success(address);
    }
}


