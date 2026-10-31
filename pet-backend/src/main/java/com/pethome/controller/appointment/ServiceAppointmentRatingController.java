package com.pethome.controller.appointment;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.entity.ServiceAppointmentRating;
import com.pethome.service.appointment.ServiceAppointmentRatingService;
import com.pethome.service.user.UserService;
import com.pethome.security.RequestIdentity;
import com.pethome.security.UserTokenResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 服务预约评价接口 - 用户对已完成服务进行评分
 */
@RestController
@RequestMapping("/api/appointment")
@Tag(name = "服务预约评价")
public class ServiceAppointmentRatingController {

    private static final Logger log = LoggerFactory.getLogger(ServiceAppointmentRatingController.class);

    @Autowired
    private ServiceAppointmentRatingService ratingService;

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private UserTokenResolver userTokenResolver;

    @PostMapping("/rating")
    @Operation(summary = "提交服务评价")
    public Result<ServiceAppointmentRating> submitRating(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        return SafeCall.of(() -> {
            Long userId = resolveUserId(authHeader);
            if (userId == null) {
                throw new BusinessException(401, "请先登录");
            }
            String appointmentType = (String) body.get("appointmentType");
            if (appointmentType == null || appointmentType.isEmpty()) {
                throw new BusinessException("预约类型不能为空");
            }
            Long appointmentId = requireLong(body.get("appointmentId"), "预约ID不能为空");
            int rating = requireInt(body.get("rating"), "评分不能为空");
            if (rating < 1 || rating > 5) {
                throw new BusinessException("评分必须在1-5之间");
            }
            String comment = body.get("comment") != null ? body.get("comment").toString().trim() : null;
            return Result.success(ratingService.submitRating(userId, appointmentType, appointmentId, rating, comment));
        }, "评价失败，请稍后重试", log);
    }

    @GetMapping("/rating/member/{memberId}")
    @Operation(summary = "获取服务人员的所有评价")
    public Result<List<Map<String, Object>>> getMemberRatings(@PathVariable Long memberId,
                                                              javax.servlet.http.HttpServletRequest request) {
        // 安全修复：评价列表对外仅展示昵称/内容，不返回评价人 userId（避免账号标识被枚举）
        boolean showUserId = RequestIdentity.isAdminOrStaff(request);
        return SafeCall.of(() -> {
            List<ServiceAppointmentRating> list = ratingService.listByMemberId(memberId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (ServiceAppointmentRating r : list) {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", r.getId());
                m.put("appointmentType", r.getAppointmentType());
                m.put("appointmentId", r.getAppointmentId());
                m.put("memberId", r.getMemberId());
                if (showUserId) {
                    m.put("userId", r.getUserId());
                }
                m.put("rating", r.getRating());
                m.put("comment", r.getComment());
                m.put("createTime", r.getCreateTime());
                String typeName = "door-cleaning".equals(r.getAppointmentType()) ? "上门铲屎"
                    : "grooming".equals(r.getAppointmentType()) ? "宠物洗护"
                    : "hospital".equals(r.getAppointmentType()) ? "宠物医院" : r.getAppointmentType();
                m.put("appointmentTypeName", typeName);
                if (r.getUserId() != null && userService != null) {
                    // 安全修复：昵称为空时不得回退到 username（历史用户 username 即手机号，会造成 PII 泄露）；
                    // 昵称查询异常由 Service 内部降级为 null，这里统一落脱敏占位
                    String nickname = userService.getNicknameSafely(r.getUserId());
                    m.put("userName", nickname != null && !nickname.isEmpty() ? nickname : "用户" + r.getUserId());
                } else {
                    m.put("userName", "匿名用户");
                }
                result.add(m);
            }
            return Result.success(result);
        }, "获取评价列表失败，请稍后重试", log);
    }

    @GetMapping("/rating/check")
    @Operation(summary = "检查是否已评价")
    public Result<Map<String, Object>> checkRated(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String appointmentType,
            @RequestParam Long appointmentId) {
        return SafeCall.of(() -> {
            Long userId = resolveUserId(authHeader);
            if (userId == null) {
                return Result.success(Map.<String, Object>of("rated", false));
            }
            return Result.success(ratingService.checkRatedQuietly(appointmentType, appointmentId, userId));
        }, "检查评价状态失败，请稍后重试", log);
    }

    /** 当前登录用户 ID（统一走 UserTokenResolver，解析失败返回 null） */
    private Long resolveUserId(String authHeader) {
        return userTokenResolver != null ? userTokenResolver.getUserIdFromBearer(authHeader) : null;
    }

    /** 解析数字参数；非数字统一返回“参数格式错误”（与历史解析失败文案一致） */
    private static Long requireLong(Object value, String missingMessage) {
        if (value == null) {
            throw new BusinessException(missingMessage);
        }
        String text = value.toString().trim();
        if (!text.matches("-?\\d{1,18}")) {
            throw new BusinessException("参数格式错误");
        }
        return Long.valueOf(text);
    }

    private static int requireInt(Object value, String missingMessage) {
        if (value == null) {
            throw new BusinessException(missingMessage);
        }
        String text = value.toString().trim();
        if (!text.matches("-?\\d{1,9}")) {
            throw new BusinessException("参数格式错误");
        }
        return Integer.parseInt(text);
    }
}
