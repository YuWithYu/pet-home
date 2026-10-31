package com.pethome.controller.community;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.entity.Complaint;
import com.pethome.service.community.ComplaintService;
import com.pethome.service.user.UserTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    @Autowired
    private UserTokenService userTokenService;

    @PostMapping("/submit")
    @Operation(summary = "提交投诉举报")
    public Result<Complaint> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String type = body.get("type") != null ? String.valueOf(body.get("type")) : null;
        String content = body.get("content") != null ? String.valueOf(body.get("content")) : null;
        String contactInfo = body.get("contactInfo") != null ? String.valueOf(body.get("contactInfo")) : null;
        // 图片列表序列化失败时降级为不带图片（由服务层兜底）
        String images = complaintService.serializeImages(body.get("images"));

        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("请输入投诉内容");
        }
        Long userId = null;
        // 优先使用拦截器写入的登录态（小程序用户为 Redis token，走不到 JWT 解析分支）
        Object attrId = request.getAttribute("userId");
        if (attrId instanceof Number) {
            userId = ((Number) attrId).longValue();
        }
        if (userId == null) {
            // 解析失败按未登录提交，不阻塞投诉
            userId = userTokenService.resolveUserId(request.getHeader("Authorization"));
        }
        Complaint c = complaintService.submit(userId, type, content, contactInfo, images);
        return Result.success(c);
    }
}
