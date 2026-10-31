package com.pethome.controller.appointment;

import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.security.RequestIdentity;
import com.pethome.service.appointment.UserAppointmentAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户端「我的预约」统一接口：一次请求返回上门铲屎、医院、洗护三类预约列表，避免三个接口都失败导致整页加载失败。
 */
@RestController
@RequestMapping("/api/user-appointments")
@Tag(name = "用户预约列表（统一）")
public class UserAppointmentController {

    private static final Logger log = LoggerFactory.getLogger(UserAppointmentController.class);

    @Autowired
    private UserAppointmentAppService userAppointmentAppService;

    @GetMapping("/list/{userId}")
    @Operation(summary = "获取当前用户全部预约（上门铲屎+医院+洗护）")
    public Result<List<Map<String, Object>>> getUserAppointments(@PathVariable Long userId, HttpServletRequest request) {
        if (userId == null || userId < 1) {
            return Result.success(Collections.emptyList());
        }
        // 安全修复：归属校验——userId 必须以登录态为准，路径参数仅兼容旧前端；
        // 未登录或与登录身份不一致时视为越权，返回空列表（与订单列表策略一致）。
        Long currentUserId = RequestIdentity.userId(request);
        if (currentUserId == null || !currentUserId.equals(userId)) {
            log.warn("预约统一列表归属校验未通过: pathUserId={}, currentUserId={}", userId, currentUserId);
            return Result.success(Collections.emptyList());
        }
        return SafeCall.of(() -> Result.success(userAppointmentAppService.listUserAppointments(userId)),
                "获取预约列表失败，请稍后重试", log);
    }
}
