package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Complaint;
import com.pethome.entity.User;
import com.pethome.service.ComplaintService;
import com.pethome.service.UserService;
import com.pethome.util.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 投诉举报 - 小程序提交接口（无需登录也可提交）
 */
@RestController
@RequestMapping("/api/complaint")
@Tag(name = "投诉举报")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;
    @Autowired(required = false)
    private JwtUtil jwtUtil;
    @Autowired(required = false)
    private UserService userService;

    @PostMapping("/submit")
    @Operation(summary = "提交投诉举报")
    public Result<Complaint> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String type = body.get("type") != null ? String.valueOf(body.get("type")) : null;
        String content = body.get("content") != null ? String.valueOf(body.get("content")) : null;
        String contactInfo = body.get("contactInfo") != null ? String.valueOf(body.get("contactInfo")) : null;
        String images = null;
        if (body.get("images") != null) {
            Object imgObj = body.get("images");
            if (imgObj instanceof String) {
                images = (String) imgObj;
            } else if (imgObj instanceof List) {
                try {
                    images = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(imgObj);
                } catch (Exception e) {
                    images = null;
                }
            }
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("请输入投诉内容");
        }
        Long userId = null;
        // 优先使用拦截器写入的登录态（小程序用户为 Redis token，走不到 JWT 解析分支）
        Object attrId = request.getAttribute("userId");
        if (attrId instanceof Number) {
            userId = ((Number) attrId).longValue();
        }
        if (userId == null && jwtUtil != null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String subject = jwtUtil.getUsernameFromToken(token.substring(7));
                    if (subject != null && userService != null) {
                        // subject 可能是 username/手机号/数字 ID，不能直接 parseLong——
                        // 手机号是纯数字会被误当成 userId，导致投诉挂到不存在的用户上
                        User user = userService.getUserByUsername(subject);
                        if (user == null) {
                            user = userService.getUserByPhone(subject);
                        }
                        if (user == null && subject.matches("\\d+")) {
                            user = userService.getUserById(Long.parseLong(subject));
                        }
                        if (user != null) {
                            userId = user.getId();
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        Complaint c = complaintService.submit(userId, type, content, contactInfo, images);
        return Result.success(c);
    }
}
