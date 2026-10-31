package com.pethome.controller.community;

import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.security.RequestIdentity;
import com.pethome.service.community.MessageAppService;
import com.pethome.service.community.MessageNotificationAppService;
import com.pethome.service.community.MessageRecommendationAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 私信管理控制器
 */
@RestController
@RequestMapping("/api/messages")
@Tag(name = "私信管理")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageAppService messageAppService;

    @Autowired
    private MessageNotificationAppService messageNotificationAppService;

    @Autowired
    private MessageRecommendationAppService messageRecommendationAppService;

    /**
     * 获取私信列表（两个用户之间的消息）
     */
    @GetMapping("/private")
    @Operation(summary = "获取私信列表")
    public Result<Map<String, Object>> getPrivateMessages(
            @RequestParam Long userId,
            @RequestParam(required = false) Long targetUserId,
            @RequestParam(required = false) String conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest httpRequest) {
        return SafeCall.of(() -> Result.success(messageAppService.getPrivateMessages(
                        userId, targetUserId, conversationId, page, size,
                        isStaffOrAdmin(httpRequest), loginUserId(httpRequest))),
                "获取私信列表失败，请稍后重试", log);
    }

    /**
     * 发送私信
     */
    @PostMapping("/private")
    @Operation(summary = "发送私信")
    public Result<Map<String, Object>> sendPrivateMessage(@RequestBody Map<String, Object> request,
                                                          HttpServletRequest httpRequest) {
        return SafeCall.of(() -> Result.success(messageAppService.sendPrivateMessage(
                        request, isStaffOrAdmin(httpRequest), loginUserId(httpRequest))),
                "发送私信失败，请稍后重试", log);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    @Operation(summary = "标记消息为已读")
    public Result<String> markMessagesAsRead(@RequestBody Map<String, Object> request,
                                             HttpServletRequest httpRequest) {
        return SafeCall.of(() -> Result.success(messageNotificationAppService.markMessagesAsRead(
                        request, isStaffOrAdmin(httpRequest), loginUserId(httpRequest))),
                "标记已读失败，请稍后重试", log);
    }

    /**
     * 获取所有客服会话列表（管理员用）
     */
    @GetMapping("/conversations/all-service")
    @Operation(summary = "获取所有客服会话列表（管理员）")
    public Result<List<Map<String, Object>>> getAllServiceConversations(
            @RequestParam(required = false) Long storeId,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest httpRequest) {
        // 安全修复：客服会话含用户的聊天内容，仅管理端角色可查看（此前任意 C 端用户传 storeId 即可读取他人会话）
        if (!RequestIdentity.isAdmin(httpRequest)) {
            return Result.error(403, "无权查看客服会话列表");
        }
        return SafeCall.of(() -> Result.success(messageAppService.getAllServiceConversations(storeId, token)),
                "获取客服会话列表失败，请稍后重试", log);
    }

    /**
     * 获取所有门店客服会话列表（服务门店，outlet_开头）
     */
    @GetMapping("/conversations/all-outlet")
    @Operation(summary = "获取门店客服会话列表（服务门店）")
    public Result<List<Map<String, Object>>> getAllOutletConversations(
            @RequestParam(required = false) Long serviceStoreId,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest httpRequest) {
        // 安全修复：非平台管理员只能查看本门店会话，忽略传入的 serviceStoreId（防止跨门店读取客服会话）
        Long storeFilter = serviceStoreId;
        if (!RequestIdentity.isPlatformAdmin(httpRequest)) {
            Long ownStoreId = RequestIdentity.serviceStoreId(httpRequest);
            if (ownStoreId == null) {
                return Result.error(403, "账号未绑定服务门店，无权查看门店客服会话");
            }
            storeFilter = ownStoreId;
        }
        final Long effectiveStoreId = storeFilter;
        return SafeCall.of(() -> Result.success(messageAppService.getAllOutletConversations(effectiveStoreId, token)),
                "获取门店客服会话列表失败，请稍后重试", log);
    }

    /**
     * 获取所有平台客服会话列表（宠物之家客服，platform_开头）
     */
    @GetMapping("/conversations/all-platform")
    @Operation(summary = "获取平台客服会话列表（宠物之家）")
    public Result<List<Map<String, Object>>> getAllPlatformConversations(
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest httpRequest) {
        // 安全修复：平台客服会话含全平台用户聊天内容，仅平台管理员可查看
        if (!RequestIdentity.isPlatformAdmin(httpRequest)) {
            return Result.error(403, "仅平台客服可查看平台会话");
        }
        return SafeCall.of(() -> Result.success(messageAppService.getAllPlatformConversations(token)),
                "获取平台客服会话列表失败，请稍后重试", log);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    public Result<List<Map<String, Object>>> getConversationList(@RequestParam Long userId,
                                                                 HttpServletRequest httpRequest) {
        return SafeCall.of(() -> Result.success(messageAppService.getConversationList(
                        userId, isStaffOrAdmin(httpRequest), loginUserId(httpRequest))),
                "获取会话列表失败，请稍后重试", log);
    }

    /**
     * 用户侧隐藏会话（与小程序「删除会话」一致：写入服务端，清缓存/换机后仍不展示）
     */
    @PostMapping("/conversations/hide")
    @Operation(summary = "隐藏会话")
    public Result<Void> hideUserConversation(@RequestBody Map<String, Object> body,
                                             HttpServletRequest httpRequest) {
        return SafeCall.of(() -> {
            messageAppService.hideUserConversation(body, isStaffOrAdmin(httpRequest), loginUserId(httpRequest));
            return Result.<Void>success(null);
        }, "隐藏会话失败，请稍后重试", log);
    }

    /**
     * 发送客服消息（店铺客服发送给用户）
     */
    @PostMapping("/service")
    @Operation(summary = "发送客服消息")
    public Result<Map<String, Object>> sendServiceMessage(@RequestBody Map<String, Object> request,
                                                          HttpServletRequest httpRequest) {
        return SafeCall.of(() -> Result.success(messageAppService.sendServiceMessage(
                        request, httpRequest.getHeader("Authorization"), RequestIdentity.isAdmin(httpRequest))),
                "发送客服消息失败，请稍后重试", log);
    }

    /**
     * 发送门店客服消息（服务门店客服发送给用户）
     */
    @PostMapping("/outlet")
    @Operation(summary = "发送门店客服消息")
    public Result<Map<String, Object>> sendOutletMessage(@RequestBody Map<String, Object> request,
                                                         HttpServletRequest httpRequest) {
        return SafeCall.of(() -> Result.success(messageAppService.sendOutletMessage(
                        request, httpRequest.getHeader("Authorization"), RequestIdentity.isAdminOrStaff(httpRequest))),
                "发送门店客服消息失败，请稍后重试", log);
    }

    /**
     * 发送平台客服消息（宠物之家客服发送给用户）
     */
    @PostMapping("/platform")
    @Operation(summary = "发送平台客服消息")
    public Result<Map<String, Object>> sendPlatformMessage(@RequestBody Map<String, Object> request,
                                                           @RequestHeader(value = "Authorization", required = false) String token,
                                                           HttpServletRequest httpRequest) {
        // 安全修复：平台客服身份（fromId=-9999）代表官方，仅平台管理员可发送，
        // 原实现任意 admin 表账号（含店长/店员）都能冒充平台客服给任意用户发消息
        if (!RequestIdentity.isPlatformAdmin(httpRequest)) {
            return Result.error(403, "仅平台客服可发送平台消息");
        }
        return SafeCall.of(() -> Result.success(messageAppService.sendPlatformMessage(request, token)),
                "发送平台客服消息失败，请稍后重试", log);
    }

    /**
     * 获取点赞和收藏列表
     */
    @GetMapping("/likes")
    @Operation(summary = "获取点赞和收藏列表")
    public Result<Map<String, Object>> getLikesAndCollections(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long lastSeenAt,
            HttpServletRequest httpRequest) {
        return SafeCall.of(() -> Result.success(messageNotificationAppService.getLikesAndCollections(
                        userId, page, size, lastSeenAt,
                        isStaffOrAdmin(httpRequest), loginUserId(httpRequest))),
                "获取点赞和收藏列表失败，请稍后重试", log);
    }

    /**
     * 获取新增关注列表
     */
    @GetMapping("/follows")
    @Operation(summary = "获取新增关注列表")
    public Result<Map<String, Object>> getNewFollows(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long lastSeenAt,
            HttpServletRequest httpRequest) {
        return SafeCall.of(() -> Result.success(messageNotificationAppService.getNewFollows(
                        userId, page, size, lastSeenAt,
                        isStaffOrAdmin(httpRequest), loginUserId(httpRequest))),
                "获取新增关注列表失败，请稍后重试", log);
    }

    /**
     * 获取评论与「@」相关通知。
     * 当前仅聚合帖子下的评论；站内 @ 提及若未单独落库，则不会出现在本接口中（与产品说明一致）。
     */
    @GetMapping("/comments")
    @Operation(summary = "获取评论和@列表")
    public Result<Map<String, Object>> getCommentsAndMentions(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long lastSeenAt,
            HttpServletRequest httpRequest) {
        return SafeCall.of(() -> Result.success(messageNotificationAppService.getCommentsAndMentions(
                        userId, page, size, lastSeenAt,
                        isStaffOrAdmin(httpRequest), loginUserId(httpRequest))),
                "获取评论和@列表失败，请稍后重试", log);
    }

    /**
     * 获取推荐用户
     */
    @GetMapping("/recommended-users")
    @Operation(summary = "获取推荐用户")
    public Result<Map<String, Object>> getRecommendedUsers(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return SafeCall.of(() -> Result.success(
                        messageRecommendationAppService.getRecommendedUsers(userId, limit)),
                "获取推荐用户失败，请稍后重试", log);
    }

    /** 从拦截器写入的登录态属性解析当前用户 ID（普通用户 Redis token） */
    private Long loginUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        return attr instanceof Number ? ((Number) attr).longValue() : null;
    }

    /** staff/admin 判定：RoleInterceptor 在管理端登录后写入 adminId attribute（客服后台需查看/代发任意会话） */
    private boolean isStaffOrAdmin(HttpServletRequest request) {
        return request.getAttribute("adminId") instanceof Number;
    }
}
