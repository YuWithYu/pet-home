package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.ServiceAppointmentRating;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.User;
import com.pethome.mapper.ServiceAppointmentRatingMapper;
import com.pethome.service.*;
import com.pethome.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 服务预约评价接口 - 用户对已完成服务进行评分
 */
@RestController
@RequestMapping("/api/appointment")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = "服务预约评价")
public class ServiceAppointmentRatingController {

    @Autowired
    private ServiceAppointmentRatingMapper ratingMapper;
    @Autowired
    private ServiceMemberService serviceMemberService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired(required = false)
    private DoorCleaningAppointmentService doorCleaningAppointmentService;
    @Autowired(required = false)
    private GroomingAppointmentService groomingAppointmentService;
    @Autowired(required = false)
    private HospitalAppointmentService hospitalAppointmentService;

    @PostMapping("/rating")
    @ApiOperation("提交服务评价")
    public Result<ServiceAppointmentRating> submitRating(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            String appointmentType = (String) body.get("appointmentType");
            Object aidObj = body.get("appointmentId");
            Object ratingObj = body.get("rating");
            String comment = body.get("comment") != null ? body.get("comment").toString().trim() : null;

            if (appointmentType == null || appointmentType.isEmpty()) {
                return Result.error("预约类型不能为空");
            }
            if (aidObj == null) {
                return Result.error("预约ID不能为空");
            }
            Long appointmentId = Long.valueOf(aidObj.toString());
            if (ratingObj == null) {
                return Result.error("评分不能为空");
            }
            int rating = Integer.parseInt(ratingObj.toString());
            if (rating < 1 || rating > 5) {
                return Result.error("评分必须在1-5之间");
            }

            Long memberId = null;
            Long aptUserId = null;

            switch (appointmentType) {
                case "door-cleaning":
                    if (doorCleaningAppointmentService != null) {
                        var apt = doorCleaningAppointmentService.getAppointmentById(appointmentId);
                        if (apt == null) return Result.error("预约不存在");
                        if (!"completed".equals(apt.getStatus())) return Result.error("只能对已完成的订单评价");
                        memberId = apt.getMemberId();
                        aptUserId = apt.getUserId();
                    }
                    break;
                case "grooming":
                    if (groomingAppointmentService != null) {
                        var apt = groomingAppointmentService.getAppointmentById(appointmentId);
                        if (apt == null) return Result.error("预约不存在");
                        if (!"completed".equals(apt.getStatus())) return Result.error("只能对已完成的订单评价");
                        memberId = apt.getMemberId();
                        aptUserId = apt.getUserId();
                    }
                    break;
                case "hospital":
                    if (hospitalAppointmentService != null) {
                        var apt = hospitalAppointmentService.getAppointmentById(appointmentId);
                        if (apt == null) return Result.error("预约不存在");
                        if (!"completed".equals(apt.getStatus())) return Result.error("只能对已完成的订单评价");
                        memberId = apt.getMemberId();
                        aptUserId = apt.getUserId();
                    }
                    break;
                default:
                    return Result.error("不支持的预约类型");
            }

            if (memberId == null) {
                return Result.error("该预约未分配服务人员，无法评价");
            }
            if (!userId.equals(aptUserId)) {
                return Result.error("只能评价自己的订单");
            }

            ServiceAppointmentRating existing = ratingMapper.findByAppointmentAndUser(appointmentType, appointmentId, userId);
            if (existing != null) {
                return Result.error("您已评价过该订单");
            }

            ServiceAppointmentRating entity = new ServiceAppointmentRating();
            entity.setAppointmentType(appointmentType);
            entity.setAppointmentId(appointmentId);
            entity.setMemberId(memberId);
            entity.setUserId(userId);
            entity.setRating(rating);
            entity.setComment(comment != null && !comment.isEmpty() ? comment : null);
            ratingMapper.insert(entity);

            BigDecimal avgRating = ratingMapper.avgRatingByMemberId(memberId);
            ServiceMember sm = serviceMemberService.getMemberById(memberId);
            if (sm != null && avgRating != null) {
                sm.setRating(avgRating);
                serviceMemberService.updateMember(sm);
            }

            return Result.success(entity);
        } catch (NumberFormatException e) {
            return Result.error("参数格式错误");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("评价失败: " + e.getMessage());
        }
    }

    @GetMapping("/rating/member/{memberId}")
    @ApiOperation("获取服务人员的所有评价")
    public Result<List<Map<String, Object>>> getMemberRatings(@PathVariable Long memberId) {
        try {
            QueryWrapper<ServiceAppointmentRating> q = new QueryWrapper<>();
            q.eq("member_id", memberId).orderByDesc("create_time");
            List<ServiceAppointmentRating> list = ratingMapper.selectList(q);
            List<Map<String, Object>> result = new ArrayList<>();
            for (ServiceAppointmentRating r : list) {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", r.getId());
                m.put("appointmentType", r.getAppointmentType());
                m.put("appointmentId", r.getAppointmentId());
                m.put("memberId", r.getMemberId());
                m.put("userId", r.getUserId());
                m.put("rating", r.getRating());
                m.put("comment", r.getComment());
                m.put("createTime", r.getCreateTime());
                String typeName = "door-cleaning".equals(r.getAppointmentType()) ? "上门铲屎"
                    : "grooming".equals(r.getAppointmentType()) ? "宠物洗护"
                    : "hospital".equals(r.getAppointmentType()) ? "宠物医院" : r.getAppointmentType();
                m.put("appointmentTypeName", typeName);
                if (r.getUserId() != null && userService != null) {
                    try {
                        User u = userService.getUserById(r.getUserId());
                        m.put("userName", u != null ? (u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername()) : "用户" + r.getUserId());
                    } catch (Exception ignored) {
                        m.put("userName", "用户" + r.getUserId());
                    }
                } else {
                    m.put("userName", "匿名用户");
                }
                result.add(m);
            }
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评价列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/rating/check")
    @ApiOperation("检查是否已评价")
    public Result<Map<String, Object>> checkRated(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String appointmentType,
            @RequestParam Long appointmentId) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return Result.success(Map.of("rated", false));
            }
            ServiceAppointmentRating r = ratingMapper.findByAppointmentAndUser(appointmentType, appointmentId, userId);
            return Result.success(Map.of("rated", r != null, "rating", r != null ? r.getRating() : 0));
        } catch (Exception e) {
            return Result.success(Map.of("rated", false));
        }
    }

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        try {
            User cachedUser = userService.getUserByToken(token);
            if (cachedUser != null) return cachedUser.getId();
        } catch (Exception ignored) {}
        try {
            Claims claims = jwtUtil.parseToken(token);
            if (claims != null) {
                String subject = claims.getSubject();
                if (subject != null) {
                    User user = userService.getUserByUsername(subject);
                    if (user == null) user = userService.getUserByPhone(subject);
                    if (user != null) return user.getId();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
