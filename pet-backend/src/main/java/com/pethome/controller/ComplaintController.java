package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Complaint;
import com.pethome.service.ComplaintService;
import com.pethome.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "投诉举报")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;
    @Autowired(required = false)
    private JwtUtil jwtUtil;

    @PostMapping("/submit")
    @ApiOperation("提交投诉举报")
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
        if (jwtUtil != null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    String subject = jwtUtil.getUsernameFromToken(token.substring(7));
                    if (subject != null) {
                        userId = Long.parseLong(subject);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        Complaint c = complaintService.submit(userId, type, content, contactInfo, images);
        return Result.success(c);
    }
}
