package com.pethome.controller.community;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.entity.*;
import com.pethome.service.order.*;
import com.pethome.service.appointment.*;
import com.pethome.service.community.*;
import com.pethome.service.user.*;
import com.pethome.service.admin.*;
import com.pethome.service.common.*;
import com.pethome.entity.Admin;
import com.pethome.security.JwtUtil;
import com.pethome.security.RequestIdentity;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * 私信管理控制器
 */
@RestController
@RequestMapping("/api/messages")
@Tag(name = "私信管理")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private IMessageService messageService;

    @Autowired
    private MessageConversationService messageConversationService;
    
    @Autowired
    private PostLikeService postLikeService;
    
    @Autowired
    private PostCollectService postCollectService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserFollowService userFollowService;
    
    @Autowired
    private PostService postService;
    
    @Autowired(required = false)
    private com.pethome.service.user.UserService userService;
    
    @Autowired(required = false)
    private com.pethome.service.order.StoreService storeService;
    
    @Autowired(required = false)
    private com.pethome.service.appointment.ServiceStoreService serviceStoreService;
    
    @Autowired(required = false)
    private com.pethome.service.admin.AdminService adminService;
    
    @Autowired(required = false)
    private JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private com.pethome.security.AdminContext adminContext;
    
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
        try {
            // 安全修复：原实现信任 query 的 userId/conversationId，任意登录用户可枚举他人会话内容。
            // 修复：非管理端强制 userId=登录态，且 conversationId 必须包含登录用户（客服后台 staff/admin 放行）。
            if (!isStaffOrAdmin(httpRequest)) {
                Long loginId = loginUserId(httpRequest);
                if (loginId == null) {
                    return Result.error(401, "用户未登录或token无效");
                }
                userId = loginId;
                if (conversationId != null && !conversationId.trim().isEmpty()
                        && !ownsConversation(conversationId.trim(), loginId)) {
                    return Result.error(403, "无权查看此会话");
                }
            }
            String finalConversationId;
            
            // 如果提供了conversationId，直接使用（用于客服消息等特殊会话）
            if (conversationId != null && !conversationId.trim().isEmpty()) {
                finalConversationId = conversationId;
            } else if (targetUserId != null) {
                // 生成会话ID（确保两个用户之间的会话ID一致）
                finalConversationId = generateConversationId(userId, targetUserId);
            } else {
                return Result.error("参数不完整：需要提供targetUserId或conversationId");
            }
            
            // 计算offset
            int offset = (page - 1) * size;
            
            // 获取消息列表
            List<Message> messages = messageService.findLetters(finalConversationId, offset, size);
            
            // 如果是客服会话，获取店铺信息
            Map<String, Object> storeInfo = null;
            if (finalConversationId != null && finalConversationId.startsWith("service_")) {
                try {
                    String storeIdStr = finalConversationId.replace("service_", "");
                    // 提取店铺ID（格式：service_storeId_userId）
                    String[] parts = storeIdStr.split("_");
                    if (parts.length > 0) {
                        Long storeId = Long.parseLong(parts[0]);
                        if (storeService != null) {
                            Store store = storeService.getById(storeId);
                            if (store != null) {
                                storeInfo = new HashMap<>();
                                storeInfo.put("storeId", storeId);
                                storeInfo.put("storeName", store.getName() != null && !store.getName().isEmpty() 
                                    ? store.getName() + "客服" : "店铺客服");
                                storeInfo.put("storeAvatar", store.getAvatar() != null && !store.getAvatar().isEmpty() 
                                    ? store.getAvatar() : "/static/images/customer-service.svg");
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取店铺信息失败", e);
                }
            }
            // 如果是平台客服会话（platform_，宠物之家客服）
            if (storeInfo == null && finalConversationId != null && finalConversationId.startsWith("platform_")) {
                storeInfo = new HashMap<>();
                storeInfo.put("storeId", 0L);
                storeInfo.put("storeName", "宠物之家客服");
                storeInfo.put("storeAvatar", "/static/images/宠物之家.png");
            }
            // 如果是门店客服会话（outlet_），获取服务门店信息；无门店图时用该门店管理员头像
            if (storeInfo == null && finalConversationId != null && finalConversationId.startsWith("outlet_")) {
                try {
                    String outletStr = finalConversationId.replace("outlet_", "");
                    String[] parts = outletStr.split("_");
                    if (parts.length > 0 && serviceStoreService != null) {
                        Long serviceStoreId = Long.parseLong(parts[0]);
                        ServiceStore ss = serviceStoreService.getStoreById(serviceStoreId);
                        if (ss != null) {
                            storeInfo = new HashMap<>();
                            storeInfo.put("storeId", serviceStoreId);
                            storeInfo.put("serviceStoreId", serviceStoreId);
                            storeInfo.put("storeName", ss.getStoreName() != null && !ss.getStoreName().isEmpty()
                                ? ss.getStoreName() + "客服" : "门店客服");
                            // 小程序端显示 = 编辑服务门店里设置的「门店头像(客服会话显示)」，未设置时回退到该门店管理员头像
                            String avatar = "/static/images/客服.png";
                            if (ss.getImageUrl() != null && !ss.getImageUrl().isEmpty()) {
                                avatar = ss.getImageUrl();
                            } else if (adminService != null) {
                                List<Admin> storeAdmins = adminService.listStaffByStoreId(serviceStoreId);
                                storeAdmins.sort(Comparator.comparing(Admin::getId, Comparator.nullsLast(Comparator.naturalOrder())));
                                for (Admin admin : storeAdmins) {
                                    if (admin.getAvatar() != null && !admin.getAvatar().isEmpty()) {
                                        avatar = admin.getAvatar();
                                        break;
                                    }
                                }
                            }
                            storeInfo.put("storeAvatar", (avatar != null && (avatar.startsWith("http://") || avatar.startsWith("https://"))) ? avatar : (avatar != null && avatar.startsWith("/") ? avatar : "/" + (avatar != null ? avatar : "")));
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取服务门店信息失败", e);
                }
            }

            // 转换为前端需要的格式
            List<Map<String, Object>> messageList = new ArrayList<>();
            for (Message msg : messages) {
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("id", msg.getId());
                msgMap.put("senderId", msg.getFromId());
                msgMap.put("receiverId", msg.getToId());
                msgMap.put("content", msg.getContent());
                msgMap.put("createTime", msg.getCreateTime());
                msgMap.put("time", formatTime(msg.getCreateTime()));
                messageList.add(msgMap);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("messages", messageList);
            result.put("total", messageService.findLetterCount(finalConversationId));
            result.put("page", page);
            result.put("size", size);
            if (storeInfo != null) {
                result.put("storeInfo", storeInfo);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取私信列表失败", e);
            return Result.error("获取私信列表失败，请稍后重试");
        }
    }
    
    /**
     * 发送私信
     */
    @PostMapping("/private")
    @Operation(summary = "发送私信")
    public Result<Map<String, Object>> sendPrivateMessage(@RequestBody Map<String, Object> request,
                                                          HttpServletRequest httpRequest) {
        try {
            Long senderId = Long.valueOf(request.get("senderId").toString());
            Long receiverId = Long.valueOf(request.get("receiverId").toString());
            String content = request.get("content").toString();
            String conversationId = request.get("conversationId") != null ? request.get("conversationId").toString() : null;

            // 安全修复：原实现信任 body 的 senderId，任意登录用户可冒充他人发私信。
            // 修复：非管理端强制 senderId=登录态，且所传 conversationId 必须归属本人（客服后台 staff/admin 放行）。
            if (!isStaffOrAdmin(httpRequest)) {
                Long loginId = loginUserId(httpRequest);
                if (loginId == null) {
                    return Result.error(401, "用户未登录或token无效");
                }
                senderId = loginId;
                if (conversationId != null && !conversationId.trim().isEmpty()
                        && !ownsConversation(conversationId.trim(), loginId)) {
                    return Result.error(403, "无权在此会话发送消息");
                }
            }

            if (senderId == null || receiverId == null) {
                return Result.error("参数不完整");
            }
            
            if (content == null || content.trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            
            // 生成会话ID（如果未提供）
            String finalConversationId;
            if (conversationId != null && !conversationId.trim().isEmpty()) {
                // 如果提供了conversationId（用于客服会话等），直接使用
                finalConversationId = conversationId;
            } else {
                // 否则生成会话ID
                finalConversationId = generateConversationId(senderId, receiverId);
            }
            
            // 创建消息
            Message message = new Message();
            message.setFromId(senderId);
            message.setToId(receiverId);
            message.setConversationId(finalConversationId);
            message.setContent(content);
            message.setStatus(0); // 0表示未读
            message.setCreateTime(new Date());
            
            // 保存消息
            int result = messageService.addMessage(message);
            
            if (result > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", message.getId());
                response.put("senderId", message.getFromId());
                response.put("receiverId", message.getToId());
                response.put("content", message.getContent());
                response.put("createTime", message.getCreateTime());
                return Result.success(response);
            } else {
                return Result.error("发送失败");
            }
        } catch (Exception e) {
            log.error("发送私信失败", e);
            return Result.error("发送私信失败，请稍后重试");
        }
    }
    
    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    @Operation(summary = "标记消息为已读")
    public Result<String> markMessagesAsRead(@RequestBody Map<String, Object> request,
                                             HttpServletRequest httpRequest) {
        return com.pethome.common.SafeCall.of(() -> {
            Long userId = Long.valueOf(request.get("userId").toString());
            String conversationId = request.get("conversationId").toString();

            // 安全修复：已读标记以登录态为准，且会话必须归属本人（防止恶意把他人会话标记已读）
            if (!isStaffOrAdmin(httpRequest)) {
                Long loginId = loginUserId(httpRequest);
                if (loginId == null) {
                    return Result.error(401, "用户未登录或token无效");
                }
                userId = loginId;
                if (conversationId != null && !conversationId.trim().isEmpty()
                        && !ownsConversation(conversationId.trim(), loginId)) {
                    return Result.error(403, "无权操作此会话");
                }
            }
            
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            if (conversationId == null || conversationId.trim().isEmpty()) {
                return Result.error("会话ID不能为空");
            }
            
            // 获取该会话的所有未读消息
            List<Message> unreadMessages = messageService.findLetters(conversationId, 0, 1000);
            
            // 过滤出当前用户（或客服）的未读消息
            // 如果userId是负数，说明是客服ID，需要标记发送给客服的未读消息
            List<Long> messageIds = new ArrayList<>();
            for (Message msg : unreadMessages) {
                if (msg.getToId() != null && msg.getToId().equals(userId) && msg.getStatus() != null && msg.getStatus() == 0) {
                    messageIds.add(msg.getId());
                }
            }
            
            // 标记为已读
            if (!messageIds.isEmpty()) {
                int result = messageService.readMessage(messageIds);
                if (result > 0) {
                    return Result.success("标记成功，已读 " + result + " 条消息");
                }
            }
            
            return Result.success("没有未读消息");
        }, "标记已读失败，请稍后重试", log);
    }
    
    /**
     * 获取所有客服会话列表（管理员用）
     */
    @GetMapping("/conversations/all-service")
    @Operation(summary = "获取所有客服会话列表（管理员）")
    public Result<List<Map<String, Object>>> getAllServiceConversations(
            @RequestParam(required = false) Long storeId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 数据隔离：获取当前登录用户的店铺ID
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        // 店铺管理员只能查看自己店铺的客服会话，忽略传入的storeId参数
        Long finalStoreId = currentStoreId != null ? currentStoreId : storeId;
        return Result.success(messageConversationService.getAllServiceConversations(finalStoreId));
    }

    /**
     * 获取所有门店客服会话列表（服务门店，outlet_开头）
     */
    @GetMapping("/conversations/all-outlet")
    @Operation(summary = "获取门店客服会话列表（服务门店）")
    public Result<List<Map<String, Object>>> getAllOutletConversations(
            @RequestParam(required = false) Long serviceStoreId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long currentServiceStoreId = adminContext != null && token != null
                ? adminContext.getCurrentServiceStoreId(token) : null;
        Long finalServiceStoreId = currentServiceStoreId != null ? currentServiceStoreId : serviceStoreId;
        return Result.success(messageConversationService.getAllOutletConversations(finalServiceStoreId));
    }

    /**
     * 获取所有平台客服会话列表（宠物之家客服，platform_开头）
     */
    @GetMapping("/conversations/all-platform")
    @Operation(summary = "获取平台客服会话列表（宠物之家）")
    public Result<List<Map<String, Object>>> getAllPlatformConversations(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Admin admin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
        if (admin == null) {
            throw new BusinessException("请先登录");
        }
        return Result.success(messageConversationService.getAllPlatformConversations());
    }
    
    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    public Result<List<Map<String, Object>>> getConversationList(@RequestParam Long userId,
                                                                 HttpServletRequest httpRequest) {
        // 安全修复：会话列表属个人数据，非管理端强制以登录态为准（防止枚举他人会话列表）
        if (!isStaffOrAdmin(httpRequest)) {
            Long loginId = loginUserId(httpRequest);
            if (loginId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            userId = loginId;
        }
        return Result.success(messageConversationService.getUserConversationList(userId));
    }

    /**
     * 用户侧隐藏会话（与小程序「删除会话」一致：写入服务端，清缓存/换机后仍不展示）
     */
    @PostMapping("/conversations/hide")
    @Operation(summary = "隐藏会话")
    public Result<Void> hideUserConversation(@RequestBody Map<String, Object> body,
                                             HttpServletRequest httpRequest) {
        return com.pethome.common.SafeCall.of(() -> {
            if (body == null || body.get("userId") == null || body.get("conversationId") == null) {
                return Result.error("参数不完整");
            }
            Long userId = Long.valueOf(body.get("userId").toString());
            String conversationId = body.get("conversationId").toString().trim();

            // 安全修复：隐藏操作以登录态为准，且会话必须归属本人（防止隐藏他人会话）
            if (!isStaffOrAdmin(httpRequest)) {
                Long loginId = loginUserId(httpRequest);
                if (loginId == null) {
                    return Result.error(401, "用户未登录或token无效");
                }
                userId = loginId;
                if (!ownsConversation(conversationId, loginId)) {
                    return Result.error(403, "无权操作此会话");
                }
            }
            if (conversationId.isEmpty()) {
                return Result.error("会话ID不能为空");
            }
            messageService.hideConversation(userId, conversationId);
            return Result.success(null);
        }, "隐藏会话失败，请稍后重试", log);
    }
    
    /**
     * 发送客服消息（店铺客服发送给用户）
     */
    @PostMapping("/service")
    @Operation(summary = "发送客服消息")
    public Result<Map<String, Object>> sendServiceMessage(@RequestBody Map<String, Object> request,
                                                         HttpServletRequest httpRequest) {
        // 安全修复：此前任意登录用户即可指定 storeId 冒充店铺客服给任意用户发私信
        if (!RequestIdentity.isAdmin(httpRequest)) {
            return Result.error(403, "无权发送客服消息");
        }
        String authHeader = httpRequest.getHeader("Authorization");
        Long ownStoreId = adminContext != null ? adminContext.getCurrentStoreId(authHeader) : null;
        boolean platformAdmin = adminContext != null && adminContext.isPlatformAdmin(authHeader);
        return com.pethome.common.SafeCall.of(() -> {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long storeId = Long.valueOf(request.get("storeId").toString());
            String content = request.get("content").toString();
            
            if (userId == null || storeId == null || content == null || content.trim().isEmpty()) {
                return Result.error("参数不完整");
            }
            
            // 非平台管理员只能以自己店铺身份发消息，防止冒充其他店铺客服
            if (!platformAdmin && (ownStoreId == null || !ownStoreId.equals(storeId))) {
                return Result.error(403, "无权以该店铺身份发送消息");
            }

            // 确保content不为空
            if (content == null) {
                return Result.error("消息内容不能为空");
            }
            
            // 生成客服会话ID（格式：service_storeId_userId）
            String conversationId = "service_" + storeId + "_" + userId;
            
            // 创建消息（fromId使用一个特殊的客服ID，比如-storeId，表示这是店铺客服）
            Message message = new Message();
            message.setFromId(-storeId); // 使用负数表示客服ID
            message.setToId(userId);
            message.setConversationId(conversationId);
            message.setContent(content);
            message.setStatus(0); // 0表示未读
            message.setCreateTime(new Date());
            
            // 保存消息
            int result = messageService.addMessage(message);
            
            if (result > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", message.getId());
                response.put("senderId", message.getFromId());
                response.put("receiverId", message.getToId());
                response.put("content", message.getContent());
                response.put("createTime", message.getCreateTime());
                response.put("isService", true);
                return Result.success(response);
            } else {
                return Result.error("发送失败");
            }
        }, "发送客服消息失败，请稍后重试", log);
    }

    /**
     * 发送门店客服消息（服务门店客服发送给用户）
     */
    @PostMapping("/outlet")
    @Operation(summary = "发送门店客服消息")
    public Result<Map<String, Object>> sendOutletMessage(@RequestBody Map<String, Object> request,
                                                        HttpServletRequest httpRequest) {
        // 安全修复：此前任意登录用户即可指定 serviceStoreId 冒充门店客服给任意用户发私信
        if (!RequestIdentity.isAdminOrStaff(httpRequest)) {
            return Result.error(403, "无权发送门店客服消息");
        }
        String authHeader = httpRequest.getHeader("Authorization");
        Long ownServiceStoreId = adminContext != null ? adminContext.getCurrentServiceStoreId(authHeader) : null;
        boolean platformAdmin = adminContext != null && adminContext.isPlatformAdmin(authHeader);
        return com.pethome.common.SafeCall.of(() -> {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long serviceStoreId = Long.valueOf(request.get("serviceStoreId").toString());
            String content = request.get("content").toString();

            if (userId == null || serviceStoreId == null || content == null || content.trim().isEmpty()) {
                return Result.error("参数不完整");
            }

            // 非平台管理员只能以自己所属服务门店身份发消息
            if (!platformAdmin && (ownServiceStoreId == null || !ownServiceStoreId.equals(serviceStoreId))) {
                return Result.error(403, "无权以该门店身份发送消息");
            }

            String conversationId = "outlet_" + serviceStoreId + "_" + userId;
            Message message = new Message();
            message.setFromId(-serviceStoreId);
            message.setToId(userId);
            message.setConversationId(conversationId);
            message.setContent(content);
            message.setStatus(0);
            message.setCreateTime(new Date());

            int result = messageService.addMessage(message);
            if (result > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", message.getId());
                response.put("senderId", message.getFromId());
                response.put("receiverId", message.getToId());
                response.put("content", message.getContent());
                response.put("createTime", message.getCreateTime());
                response.put("isService", true);
                response.put("isOutlet", true);
                return Result.success(response);
            }
            return Result.error("发送失败");
        }, "发送门店客服消息失败，请稍后重试", log);
    }

    /**
     * 发送平台客服消息（宠物之家客服发送给用户）
     */
    @PostMapping("/platform")
    @Operation(summary = "发送平台客服消息")
    public Result<Map<String, Object>> sendPlatformMessage(@RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            Admin admin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
            if (admin == null) {
                return Result.error("请先登录");
            }
            Long userId = Long.valueOf(request.get("userId").toString());
            String content = request.get("content").toString();
            if (userId == null || content == null || content.trim().isEmpty()) {
                return Result.error("参数不完整");
            }
            String conversationId = "platform_" + userId;
            Message message = new Message();
            message.setFromId(-9999L);
            message.setToId(userId);
            message.setConversationId(conversationId);
            message.setContent(content);
            message.setStatus(0);
            message.setCreateTime(new Date());
            int result = messageService.addMessage(message);
            if (result > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", message.getId());
                response.put("senderId", message.getFromId());
                response.put("receiverId", message.getToId());
                response.put("content", message.getContent());
                response.put("createTime", message.getCreateTime());
                response.put("isService", true);
                response.put("isPlatform", true);
                return Result.success(response);
            }
            return Result.error("发送失败");
        }, "发送平台客服消息失败，请稍后重试", log);
    }
    
    /**
     * 生成会话ID（确保两个用户之间的会话ID一致）
     */
    private String generateConversationId(Long userId1, Long userId2) {
        // 确保较小的ID在前，这样两个用户之间的会话ID是一致的
        if (userId1 < userId2) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
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

    /**
     * 会话归属校验：conversationId 各数字分段中必须包含登录用户 ID（作为参与方）。
     * 支持格式：用户会话 min_max / 客服会话 service_{storeId}_{userId} / outlet_{store}_{userId} / platform_{userId}
     */
    private boolean ownsConversation(String conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return false;
        }
        for (String part : conversationId.split("_")) {
            try {
                if (userId.equals(Long.parseLong(part.trim()))) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
                // 非数字分段跳过
            }
        }
        return false;
    }
    
    /**
     * 格式化时间
     */
    private String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(date);
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
        try {
            // 安全修复：互动通知属个人数据，非管理端强制以登录态为准（防止枚举他人收到的点赞/收藏）
            if (!isStaffOrAdmin(httpRequest)) {
                userId = loginUserId(httpRequest);
            }
            if (userId == null) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("likes", new ArrayList<>());
                empty.put("total", 0);
                empty.put("page", page);
                empty.put("size", size);
                return Result.success(empty);
            }
            List<Map<String, Object>> likesList = new ArrayList<>();
            
            // 获取该用户所有帖子的ID
            QueryWrapper<Post> postWrapper = new QueryWrapper<>();
            postWrapper.eq("user_id", userId);
            postWrapper.eq("status", 1);
            List<Post> userPosts = postService.list(postWrapper);
            List<Long> postIds = userPosts.stream().map(Post::getId).collect(Collectors.toList());
            
            if (postIds.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("likes", likesList);
                result.put("total", 0);
                result.put("page", page);
                result.put("size", size);
                return Result.success(result);
            }
            
            // 获取点赞记录
            QueryWrapper<PostLike> likeWrapper = new QueryWrapper<>();
            likeWrapper.in("post_id", postIds);
            likeWrapper.orderByDesc("create_time");
            List<PostLike> likes = postLikeService.list(likeWrapper);
            
            // 获取收藏记录
            QueryWrapper<PostCollect> collectWrapper = new QueryWrapper<>();
            collectWrapper.in("post_id", postIds);
            collectWrapper.orderByDesc("create_time");
            List<PostCollect> collects = new ArrayList<>();
            try {
                if (postCollectService instanceof com.baomidou.mybatisplus.extension.service.IService) {
                    @SuppressWarnings("unchecked")
                    com.baomidou.mybatisplus.extension.service.IService<PostCollect> service = 
                        (com.baomidou.mybatisplus.extension.service.IService<PostCollect>) postCollectService;
                    collects = service.list(collectWrapper);
                }
            } catch (Exception e) {
                // 如果PostCollect不存在，忽略
                log.warn("查询收藏列表失败（可能表不存在）", e);
            }
            
            LocalDateTime lastSeenTime = lastSeenAt != null && lastSeenAt > 0
                    ? LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSeenAt), ZoneId.systemDefault()) : null;
            
            // 合并点赞和收藏记录（只统计别人对我的：排除自己点赞/收藏自己的帖子）
            for (PostLike like : likes) {
                if (userId.equals(like.getUserId())) continue;
                Map<String, Object> item = new HashMap<>();
                item.put("id", like.getId());
                item.put("type", "like");
                item.put("postId", like.getPostId());
                item.put("userId", like.getUserId());
                item.put("createTime", like.getCreateTime());
                
                // 获取用户信息
                if (userService != null) {
                    try {
                        com.pethome.entity.User user = userService.getUserById(like.getUserId());
                        if (user != null) {
                            item.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                            item.put("userAvatar", user.getAvatar());
                        }
                    } catch (Exception e) {
                        // 忽略
                    }
                }
                
                // 获取帖子信息
                Post post = postService.getById(like.getPostId());
                if (post != null) {
                    item.put("postContent", post.getContent());
                    if (post.getImages() != null && !post.getImages().isEmpty()) {
                        String[] images = post.getImages().split(",");
                        if (images.length > 0) {
                            item.put("postImage", images[0]);
                        }
                    }
                }
                
                likesList.add(item);
            }
            
            // 添加收藏记录（排除自己收藏自己的帖子）
            for (PostCollect collect : collects) {
                if (collect == null || userId.equals(collect.getUserId())) {
                    continue;
                }
                Map<String, Object> item = new HashMap<>();
                item.put("id", collect.getId() != null ? collect.getId() : 0L);
                item.put("type", "collect");
                item.put("postId", collect.getPostId() != null ? collect.getPostId() : 0L);
                item.put("userId", collect.getUserId() != null ? collect.getUserId() : 0L);
                item.put("createTime", collect.getCreateTime());
                
                // 获取用户信息
                if (userService != null) {
                    try {
                        com.pethome.entity.User user = userService.getUserById(collect.getUserId());
                        if (user != null) {
                            item.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                            item.put("userAvatar", user.getAvatar());
                        }
                    } catch (Exception e) {
                        // 忽略
                    }
                }
                
                // 获取帖子信息
                Long postId = collect.getPostId();
                if (postId != null) {
                    Post post = postService.getById(postId);
                    if (post != null) {
                        item.put("postContent", post.getContent());
                        if (post.getImages() != null && !post.getImages().isEmpty()) {
                            String[] images = post.getImages().split(",");
                            if (images.length > 0) {
                                item.put("postImage", images[0]);
                            }
                        }
                    }
                }
                
                likesList.add(item);
            }
            
            // 按时间排序
            likesList.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("createTime");
                LocalDateTime timeB = (LocalDateTime) b.get("createTime");
                if (timeA == null || timeB == null) return 0;
                return timeB.compareTo(timeA);
            });
            
            // 分页；未读数：传了 lastSeenAt 时只统计该时间之后的条数
            int offset = (page - 1) * size;
            int total = lastSeenTime == null ? likesList.size()
                    : (int) likesList.stream().filter(m -> {
                        LocalDateTime t = (LocalDateTime) m.get("createTime");
                        return t != null && t.isAfter(lastSeenTime);
                    }).count();
            List<Map<String, Object>> pagedList = likesList.stream()
                    .skip(offset)
                    .limit(size)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("likes", pagedList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取点赞和收藏列表失败", e);
            return Result.error("获取点赞和收藏列表失败，请稍后重试");
        }
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
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：关注通知属个人数据，非管理端强制以登录态为准
            Long resolvedUserId = userId;
            if (!isStaffOrAdmin(httpRequest)) {
                resolvedUserId = loginUserId(httpRequest);
            }
            final Long targetUserId = resolvedUserId;
            if (targetUserId == null) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("follows", new ArrayList<>());
                empty.put("total", 0);
                empty.put("page", page);
                empty.put("size", size);
                return Result.success(empty);
            }
            LocalDateTime lastSeenTime = lastSeenAt != null && lastSeenAt > 0
                    ? LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSeenAt), ZoneId.systemDefault()) : null;
            // 查询关注该用户的记录（follower_id是关注者，following_id是被关注者）= 别人关注我
            QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
            wrapper.eq("following_id", targetUserId);
            wrapper.orderByDesc("create_time");
            
            List<UserFollow> follows = userFollowService.list(wrapper);
            
            List<Map<String, Object>> followsList = new ArrayList<>();
            
            for (UserFollow follow : follows) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", follow.getId());
                item.put("userId", follow.getFollowerId());
                item.put("createTime", follow.getCreateTime());
                
                // 获取用户信息
                if (userService != null) {
                    try {
                        com.pethome.entity.User user = userService.getUserById(follow.getFollowerId());
                        if (user != null) {
                            item.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                            item.put("userAvatar", user.getAvatar());
                        }
                    } catch (Exception e) {
                        // 忽略
                    }
                }
                
                // 检查当前用户是否已关注该用户（回粉状态）
                boolean isFollowed = userFollowService.isFollowing(targetUserId, follow.getFollowerId());
                item.put("isFollowed", isFollowed);
                
                followsList.add(item);
            }
            
            // 分页；未读数：传了 lastSeenAt 时只统计该时间之后的条数
            int offset = (page - 1) * size;
            int total = lastSeenTime == null ? followsList.size()
                    : (int) followsList.stream().filter(m -> {
                        LocalDateTime t = (LocalDateTime) m.get("createTime");
                        return t != null && t.isAfter(lastSeenTime);
                    }).count();
            List<Map<String, Object>> pagedList = followsList.stream()
                    .skip(offset)
                    .limit(size)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("follows", pagedList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            
            return Result.success(result);
        }, "获取新增关注列表失败，请稍后重试", log);
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
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：评论通知属个人数据，非管理端强制以登录态为准
            Long resolvedUserId = userId;
            if (!isStaffOrAdmin(httpRequest)) {
                resolvedUserId = loginUserId(httpRequest);
            }
            final Long targetUserId = resolvedUserId;
            if (targetUserId == null) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("comments", new ArrayList<>());
                empty.put("total", 0);
                empty.put("page", page);
                empty.put("size", size);
                return Result.success(empty);
            }
            LocalDateTime lastSeenTime = lastSeenAt != null && lastSeenAt > 0
                    ? LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSeenAt), ZoneId.systemDefault()) : null;
            // 获取该用户所有帖子的ID
            QueryWrapper<Post> postWrapper = new QueryWrapper<>();
            postWrapper.eq("user_id", targetUserId);
            postWrapper.eq("status", 1);
            List<Post> userPosts = postService.list(postWrapper);
            List<Long> postIds = userPosts.stream().map(Post::getId).collect(Collectors.toList());

            if (postIds.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("comments", new ArrayList<>());
                result.put("total", 0);
                result.put("page", page);
                result.put("size", size);
                return Result.success(result);
            }

            // 获取评论记录
            QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
            commentWrapper.in("post_id", postIds);
            commentWrapper.eq("status", 1);
            commentWrapper.orderByDesc("create_time");
            List<Comment> comments = commentService.list(commentWrapper);
            
            List<Map<String, Object>> commentsList = new ArrayList<>();
            
            for (Comment comment : comments) {
                // 只统计别人对我的：排除自己评论自己的帖子
                if (targetUserId.equals(comment.getUserId())) continue;
                Map<String, Object> item = new HashMap<>();
                item.put("id", comment.getId());
                item.put("type", "comment");
                item.put("postId", comment.getPostId());
                item.put("userId", comment.getUserId());
                item.put("content", comment.getContent());
                item.put("createTime", comment.getCreateTime());
                
                // 获取用户信息
                if (userService != null) {
                    try {
                        com.pethome.entity.User user = userService.getUserById(comment.getUserId());
                        if (user != null) {
                            item.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                            item.put("userAvatar", user.getAvatar());
                        }
                    } catch (Exception e) {
                        // 忽略
                    }
                }
                
                // 获取帖子信息
                Post post = postService.getById(comment.getPostId());
                if (post != null) {
                    item.put("postContent", post.getContent());
                    if (post.getImages() != null && !post.getImages().isEmpty()) {
                        String[] images = post.getImages().split(",");
                        if (images.length > 0) {
                            item.put("postImage", images[0]);
                        }
                    }
                }
                
                commentsList.add(item);
            }
            
            // @ 提及未单独建模时无法列出；此处仅返回评论流，与接口说明一致
            
            // 分页；未读数：传了 lastSeenAt 时只统计该时间之后的条数
            int offset = (page - 1) * size;
            int total = lastSeenTime == null ? commentsList.size()
                    : (int) commentsList.stream().filter(m -> {
                        LocalDateTime t = (LocalDateTime) m.get("createTime");
                        return t != null && t.isAfter(lastSeenTime);
                    }).count();
            List<Map<String, Object>> pagedList = commentsList.stream()
                    .skip(offset)
                    .limit(size)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("comments", pagedList);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            
            return Result.success(result);
        }, "获取评论和@列表失败，请稍后重试", log);
    }
    
    /**
     * 获取推荐用户
     */
    @GetMapping("/recommended-users")
    @Operation(summary = "获取推荐用户")
    public Result<Map<String, Object>> getRecommendedUsers(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> usersList = new ArrayList<>();
            
            if (userService == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("users", usersList);
                return Result.success(result);
            }
            
            // 获取当前用户已关注的用户ID列表
            List<UserFollow> followingList = userFollowService.getFollowingList(userId);
            Set<Long> followingIds = followingList.stream()
                    .map(UserFollow::getFollowingId)
                    .collect(Collectors.toSet());
            followingIds.add(userId); // 排除自己
            
            // 从数据库查询可推荐用户：状态正常、排除自己，按 id 倒序取候选后再过滤已关注
            List<com.pethome.entity.User> users = new ArrayList<>();
            if (userService != null) {
                int wanted = Math.min(Math.max(limit != null ? limit : 10, 1), 50);
                // 多取一些候选，保证过滤掉已关注后仍能凑够 wanted 个
                int candidateLimit = Math.min(wanted + followingIds.size(), 200);
                List<com.pethome.entity.User> candidates = userService.getRecentUsers(userId, true, candidateLimit);
                for (com.pethome.entity.User candidate : candidates) {
                    if (candidate == null || candidate.getId() == null) {
                        continue;
                    }
                    if (followingIds.contains(candidate.getId())) {
                        continue;
                    }
                    users.add(candidate);
                    if (users.size() >= wanted) {
                        break;
                    }
                }
            }
            
            for (com.pethome.entity.User user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("nickname", user.getNickname());
                userMap.put("username", user.getUsername());
                userMap.put("avatar", user.getAvatar());
                userMap.put("gender", user.getGender());
                
                // 获取粉丝数
                int fansCount = userFollowService.getFollowerCount(user.getId());
                userMap.put("fansCount", fansCount);
                
                // 获取点赞数（需要从帖子点赞统计）
                try {
                    QueryWrapper<Post> postWrapper = new QueryWrapper<>();
                    postWrapper.eq("user_id", user.getId());
                    postWrapper.eq("status", 1);
                    List<Post> userPosts = postService.list(postWrapper);
                    long totalLikes = 0;
                    for (Post post : userPosts) {
                        QueryWrapper<PostLike> likeWrapper = new QueryWrapper<>();
                        likeWrapper.eq("post_id", post.getId());
                        totalLikes += postLikeService.count(likeWrapper);
                    }
                    userMap.put("likeCount", (int) totalLikes);
                } catch (Exception e) {
                    userMap.put("likeCount", 0);
                }
                
                // 获取帖子数
                try {
                    QueryWrapper<Post> postWrapper = new QueryWrapper<>();
                    postWrapper.eq("user_id", user.getId());
                    postWrapper.eq("status", 1);
                    long postCount = postService.count(postWrapper);
                    userMap.put("postCount", (int) postCount);
                } catch (Exception e) {
                    userMap.put("postCount", 0);
                }
                
                // 检查是否已关注
                boolean isFollowed = userFollowService.isFollowing(userId, user.getId());
                userMap.put("isFollowed", isFollowed);
                
                usersList.add(userMap);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("users", usersList);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取推荐用户失败", e);
            return Result.error("获取推荐用户失败，请稍后重试");
        }
    }

}

