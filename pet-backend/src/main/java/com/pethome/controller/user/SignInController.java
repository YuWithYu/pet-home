package com.pethome.controller.user;


import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.security.UserTokenResolver;
import com.pethome.service.user.SignInService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 签到控制器
 */
@RestController
@RequestMapping("/api/signin")
@Tag(name = "签到管理")
public class SignInController {

    private static final Logger log = LoggerFactory.getLogger(SignInController.class);

    @Autowired
    private SignInService signInService;

    @Autowired
    private UserTokenResolver userTokenResolver;

    @GetMapping("/data")
    @Operation(summary = "获取签到数据")
    public Result<Map<String, Object>> getSignInData(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return SafeCall.of(() -> {
            Long userId = userTokenResolver.getUserIdFromBearer(authHeader);
            
            if (userId == null) {
                // 未登录用户返回默认数据
                Map<String, Object> data = new HashMap<>();
                data.put("balance", 0);
                data.put("consecutiveDays", 0);
                data.put("canSignIn", false);
                data.put("calendar", new boolean[]{false, false, false, false, false, false, false});
                return Result.success("获取签到数据成功", data);
            }
            
            // 使用真实服务获取签到数据
            Map<String, Object> data = signInService.getSignInData(userId);
            
            return Result.success("获取签到数据成功", data);
        }, "获取签到数据失败，请稍后重试", log);
    }

    @PostMapping("/do")
    @Operation(summary = "执行签到")
    public Result<Map<String, Object>> doSignIn(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return SafeCall.of(() -> {
            Long userId = userTokenResolver.getUserIdFromBearer(authHeader);
            
            if (userId == null) {
                throw new BusinessException(401, "未登录或token无效");
            }
            
            // 使用真实服务执行签到
            Map<String, Object> data = signInService.doSignIn(userId);
            
            return Result.success("签到成功", data);
        }, "签到失败，请稍后重试", log);
    }
}
