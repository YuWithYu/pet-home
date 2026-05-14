package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.Result;
import com.pethome.entity.*;
import com.pethome.service.*;
import com.pethome.mapper.StoreMapper;
import com.pethome.mapper.MessageMapper;
import com.pethome.entity.Admin;
import com.pethome.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 私信管理控制器
 */
@RestController
@RequestMapping("/api/messages")
@Api(tags = "私信管理")
public class MessageController {
    
    @Autowired
    private IMessageService messageService;
    
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
    private com.pethome.service.UserService userService;
    
    @Autowired(required = false)
    private StoreMapper storeMapper;

    @Autowired(required = false)
    private com.pethome.mapper.ServiceStoreMapper serviceStoreMapper;
    
    @Autowired(required = false)
    private com.pethome.service.ServiceStoreService serviceStoreService;
    
    @Autowired(required = false)
    private com.pethome.service.AdminService adminService;
    
    @Autowired(required = false)
    private MessageMapper messageMapper;
    
    @Autowired(required = false)
    private JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private com.pethome.util.AdminContext adminContext;

    @Autowired(required = false)
    private com.pethome.mapper.UserMapper userMapper;
    
    /**
     * 获取私信列表（两个用户之间的消息）
     */
    @GetMapping("/private")
    @ApiOperation("获取私信列表")
    public Result<Map<String, Object>> getPrivateMessages(
            @RequestParam Long userId,
            @RequestParam(required = false) Long targetUserId,
            @RequestParam(required = false) String conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
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
                        if (storeMapper != null) {
                            Store store = storeMapper.selectById(storeId);
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
                    System.err.println("获取店铺信息失败: " + e.getMessage());
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
                    if (parts.length > 0 && serviceStoreMapper != null) {
                        Long serviceStoreId = Long.parseLong(parts[0]);
                        ServiceStore ss = serviceStoreMapper.selectById(serviceStoreId);
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
                    System.err.println("获取服务门店信息失败: " + e.getMessage());
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
            e.printStackTrace();
            return Result.error("获取私信列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送私信
     */
    @PostMapping("/private")
    @ApiOperation("发送私信")
    public Result<Map<String, Object>> sendPrivateMessage(@RequestBody Map<String, Object> request) {
        try {
            Long senderId = Long.valueOf(request.get("senderId").toString());
            Long receiverId = Long.valueOf(request.get("receiverId").toString());
            String content = request.get("content").toString();
            String conversationId = request.get("conversationId") != null ? request.get("conversationId").toString() : null;
            
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
            e.printStackTrace();
            return Result.error("发送私信失败: " + e.getMessage());
        }
    }
    
    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    @ApiOperation("标记消息为已读")
    public Result<String> markMessagesAsRead(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String conversationId = request.get("conversationId").toString();
            
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记已读失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有客服会话列表（管理员用）
     */
    @GetMapping("/conversations/all-service")
    @ApiOperation("获取所有客服会话列表（管理员）")
    public Result<List<Map<String, Object>>> getAllServiceConversations(
            @RequestParam(required = false) Long storeId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：获取当前登录用户的店铺ID
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            
            // 如果是店铺管理员，只能查看自己店铺的会话
            Long finalStoreId = storeId;
            if (currentStoreId != null) {
                // 店铺管理员只能查看自己店铺的客服会话，忽略传入的storeId参数
                finalStoreId = currentStoreId;
            }
            
            // 直接通过SQL查询所有客服会话（优化性能，避免循环查询）
            List<Message> allMessages = new ArrayList<>();
            if (messageMapper != null) {
                if (finalStoreId != null) {
                    // 如果指定了店铺ID，只查询该店铺的客服会话
                    allMessages = messageMapper.selectServiceConversationsByStoreId(finalStoreId);
                } else {
                    // 查询所有客服会话
                    allMessages = messageMapper.selectAllServiceConversations();
                }
            } else {
                // 如果mapper不可用，返回空列表
                return Result.success(new ArrayList<>());
            }
            
            // 用于存储唯一的会话
            Map<String, Map<String, Object>> conversationMap = new HashMap<>();
            
            for (Message msg : allMessages) {
                String conversationId = msg.getConversationId();
                if (conversationId == null || !conversationId.startsWith("service_")) {
                    continue; // 跳过非客服会话
                }
                
                // 如果会话已存在，更新最后消息时间
                if (conversationMap.containsKey(conversationId)) {
                    Map<String, Object> existingConv = conversationMap.get(conversationId);
                    Date lastTime = (Date) existingConv.get("lastMessageTime");
                    if (msg.getCreateTime() != null && (lastTime == null || msg.getCreateTime().after(lastTime))) {
                        existingConv.put("lastMessage", msg.getContent());
                        existingConv.put("lastMessageTime", msg.getCreateTime());
                    }
                } else {
                    // 创建新会话
                    Map<String, Object> convMap = new HashMap<>();
                    convMap.put("conversationId", conversationId);
                    convMap.put("isService", true);
                    
                    // 解析店铺ID和用户ID
                    String storeIdStr = conversationId.replace("service_", "");
                    String[] parts = storeIdStr.split("_");
                    Long msgStoreId = 1L; // 默认店铺ID
                    Long userId = null;
                    
                    if (parts.length >= 2) {
                        try {
                            msgStoreId = Long.parseLong(parts[0]);
                            userId = Long.parseLong(parts[1]);
                        } catch (NumberFormatException e) {
                            // 忽略解析错误
                        }
                    } else if (parts.length == 1) {
                        // 如果只有一个部分，可能是旧的格式，尝试解析
                        try {
                            msgStoreId = Long.parseLong(parts[0]);
                        } catch (NumberFormatException e) {
                            // 忽略
                        }
                    }
                    
                    convMap.put("storeId", msgStoreId);
                    convMap.put("otherUserId", userId);
                    convMap.put("userId", userId);
                    
                    // 获取店铺信息
                    String storeName = "店铺客服";
                    String storeAvatar = "/static/images/customer-service.svg";
                    if (storeMapper != null) {
                        try {
                            Store store = storeMapper.selectById(msgStoreId);
                            if (store != null) {
                                if (store.getName() != null && !store.getName().isEmpty()) {
                                    storeName = store.getName() + "客服";
                                }
                                if (store.getAvatar() != null && !store.getAvatar().isEmpty()) {
                                    storeAvatar = store.getAvatar();
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("获取店铺信息失败: " + e.getMessage());
                        }
                    }
                    convMap.put("otherUserName", storeName);
                    convMap.put("storeName", storeName);
                    convMap.put("otherUserAvatar", storeAvatar);
                    convMap.put("storeAvatar", storeAvatar);
                    
                    // 获取用户信息（会话列表中应该显示用户信息，而不是店铺信息）
                    if (userId != null && userService != null) {
                        try {
                            com.pethome.entity.User user = userService.getUserById(userId);
                            if (user != null) {
                                convMap.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                                convMap.put("userNickname", user.getNickname());
                                convMap.put("userAvatar", user.getAvatar());
                            }
                        } catch (Exception e) {
                            // 忽略
                        }
                    }
                    
                    convMap.put("lastMessage", msg.getContent());
                    convMap.put("lastMessageTime", msg.getCreateTime());
                    
                    // 计算未读消息数（客服未读的消息，即用户发送给客服的未读消息）
                    // 对于客服会话，未读消息应该是：toId = -storeId 且 status = 0 的消息
                    int unreadCount = 0;
                    try {
                        Long serviceId = -msgStoreId; // 客服ID是负数
                        // 使用mapper直接查询未读消息数（优化性能）
                        if (messageMapper != null) {
                            unreadCount = messageMapper.selectLetterUnreadCount(serviceId, conversationId);
                        } else {
                            // 如果mapper不可用，使用service方法（较慢）
                            List<Message> conversationMessages = messageService.findLetters(conversationId, 0, 1000);
                            for (Message message : conversationMessages) {
                                if (message.getToId() != null && message.getToId().equals(serviceId) 
                                    && message.getStatus() != null && message.getStatus() == 0) {
                                    unreadCount++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 忽略
                        System.err.println("计算未读消息数失败: " + e.getMessage());
                    }
                    convMap.put("unreadCount", unreadCount);
                    
                    conversationMap.put(conversationId, convMap);
                }
            }
            
            // 转换为列表并按时间排序
            List<Map<String, Object>> conversationList = new ArrayList<>(conversationMap.values());
            conversationList.sort((a, b) -> {
                Date timeA = (Date) a.get("lastMessageTime");
                Date timeB = (Date) b.get("lastMessageTime");
                if (timeA == null && timeB == null) return 0;
                if (timeA == null) return 1;
                if (timeB == null) return -1;
                return timeB.compareTo(timeA); // 降序，最新的在前
            });
            
            return Result.success(conversationList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取客服会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有门店客服会话列表（服务门店，outlet_开头）
     */
    @GetMapping("/conversations/all-outlet")
    @ApiOperation("获取门店客服会话列表（服务门店）")
    public Result<List<Map<String, Object>>> getAllOutletConversations(
            @RequestParam(required = false) Long serviceStoreId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentServiceStoreId = null;
            if (adminContext != null && token != null) {
                currentServiceStoreId = adminContext.getCurrentServiceStoreId(token);
            }
            Long finalServiceStoreId = currentServiceStoreId != null ? currentServiceStoreId : serviceStoreId;

            List<Message> allMessages = new ArrayList<>();
            if (messageMapper != null) {
                if (finalServiceStoreId != null) {
                    allMessages = messageMapper.selectOutletConversationsByServiceStoreId(finalServiceStoreId);
                } else {
                    allMessages = messageMapper.selectAllOutletConversations();
                }
            }

            Map<String, Map<String, Object>> conversationMap = new HashMap<>();
            for (Message msg : allMessages) {
                String conversationId = msg.getConversationId();
                if (conversationId == null || !conversationId.startsWith("outlet_")) continue;

                if (conversationMap.containsKey(conversationId)) {
                    Map<String, Object> existing = conversationMap.get(conversationId);
                    Date lastTime = (Date) existing.get("lastMessageTime");
                    if (msg.getCreateTime() != null && (lastTime == null || msg.getCreateTime().after(lastTime))) {
                        existing.put("lastMessage", msg.getContent());
                        existing.put("lastMessageTime", msg.getCreateTime());
                    }
                } else {
                    Map<String, Object> convMap = new HashMap<>();
                    convMap.put("conversationId", conversationId);
                    convMap.put("isService", true);
                    convMap.put("isOutlet", true);

                    String outletStr = conversationId.replace("outlet_", "");
                    String[] parts = outletStr.split("_");
                    Long msgServiceStoreId = 1L;
                    Long userId = null;
                    if (parts.length >= 2) {
                        try {
                            msgServiceStoreId = Long.parseLong(parts[0]);
                            userId = Long.parseLong(parts[1]);
                        } catch (NumberFormatException ignored) {}
                    }
                    convMap.put("serviceStoreId", msgServiceStoreId);
                    convMap.put("storeId", msgServiceStoreId);
                    convMap.put("userId", userId);
                    convMap.put("otherUserId", userId);

                    String storeName = "门店客服";
                    String storeAvatar = "/static/images/customer-service.svg";
                    if (serviceStoreMapper != null) {
                        try {
                            ServiceStore ss = serviceStoreMapper.selectById(msgServiceStoreId);
                            if (ss != null) {
                                storeName = (ss.getStoreName() != null && !ss.getStoreName().isEmpty() ? ss.getStoreName() : "门店") + "客服";
                                // 小程序端显示 = 编辑服务门店里「门店头像(客服会话显示)」，未设置时回退管理员头像
                                if (ss.getImageUrl() != null && !ss.getImageUrl().isEmpty()) {
                                    String img = ss.getImageUrl();
                                    storeAvatar = (img.startsWith("http://") || img.startsWith("https://")) ? img : (img.startsWith("/") ? img : "/" + img);
                                } else if (adminService != null) {
                                    List<Admin> storeAdmins = adminService.listStaffByStoreId(msgServiceStoreId);
                                    storeAdmins.sort(Comparator.comparing(Admin::getId, Comparator.nullsLast(Comparator.naturalOrder())));
                                    for (Admin admin : storeAdmins) {
                                        if (admin.getAvatar() != null && !admin.getAvatar().isEmpty()) {
                                            String a = admin.getAvatar();
                                            storeAvatar = (a.startsWith("http://") || a.startsWith("https://")) ? a : (a.startsWith("/") ? a : "/" + a);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                    convMap.put("storeName", storeName);
                    convMap.put("otherUserName", storeName);
                    convMap.put("storeAvatar", storeAvatar);
                    convMap.put("otherUserAvatar", storeAvatar);

                    if (userId != null && userService != null) {
                        try {
                            com.pethome.entity.User user = userService.getUserById(userId);
                            if (user != null) {
                                convMap.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                                convMap.put("userNickname", user.getNickname());
                                convMap.put("userAvatar", user.getAvatar());
                            }
                        } catch (Exception ignored) {}
                    }

                    convMap.put("lastMessage", msg.getContent());
                    convMap.put("lastMessageTime", msg.getCreateTime());

                    int unreadCount = 0;
                    try {
                        Long serviceId = -msgServiceStoreId;
                        unreadCount = messageMapper != null ? messageMapper.selectLetterUnreadCount(serviceId, conversationId) : 0;
                    } catch (Exception ignored) {}
                    convMap.put("unreadCount", unreadCount);
                    conversationMap.put(conversationId, convMap);
                }
            }

            List<Map<String, Object>> conversationList = new ArrayList<>(conversationMap.values());
            conversationList.sort((a, b) -> {
                Date timeA = (Date) a.get("lastMessageTime");
                Date timeB = (Date) b.get("lastMessageTime");
                if (timeA == null && timeB == null) return 0;
                if (timeA == null) return 1;
                if (timeB == null) return -1;
                return timeB.compareTo(timeA);
            });
            return Result.success(conversationList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取门店客服会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有平台客服会话列表（宠物之家客服，platform_开头）
     */
    @GetMapping("/conversations/all-platform")
    @ApiOperation("获取平台客服会话列表（宠物之家）")
    public Result<List<Map<String, Object>>> getAllPlatformConversations(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Admin admin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
            if (admin == null) {
                return Result.error("请先登录");
            }
            List<Message> allMessages = messageMapper != null ? messageMapper.selectAllPlatformConversations() : new ArrayList<>();
            Map<String, Map<String, Object>> conversationMap = new HashMap<>();
            for (Message msg : allMessages) {
                String conversationId = msg.getConversationId();
                if (conversationId == null || !conversationId.startsWith("platform_")) continue;
                if (conversationMap.containsKey(conversationId)) {
                    Map<String, Object> existing = conversationMap.get(conversationId);
                    Date lastTime = (Date) existing.get("lastMessageTime");
                    if (msg.getCreateTime() != null && (lastTime == null || msg.getCreateTime().after(lastTime))) {
                        existing.put("lastMessage", msg.getContent());
                        existing.put("lastMessageTime", msg.getCreateTime());
                    }
                } else {
                    Map<String, Object> convMap = new HashMap<>();
                    convMap.put("conversationId", conversationId);
                    convMap.put("isService", true);
                    convMap.put("isPlatform", true);
                    convMap.put("storeId", 0L);
                    String platformStr = conversationId.replace("platform_", "");
                    Long userId = null;
                    try {
                        userId = Long.parseLong(platformStr);
                    } catch (NumberFormatException ignored) {}
                    convMap.put("userId", userId);
                    convMap.put("otherUserId", userId);
                    convMap.put("storeName", "宠物之家客服");
                    convMap.put("otherUserName", "宠物之家客服");
                    convMap.put("storeAvatar", "/static/images/宠物之家.png");
                    convMap.put("otherUserAvatar", "/static/images/宠物之家.png");
                    if (userId != null && userService != null) {
                        try {
                            com.pethome.entity.User user = userService.getUserById(userId);
                            if (user != null) {
                                convMap.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                                convMap.put("userNickname", user.getNickname());
                                convMap.put("userAvatar", user.getAvatar());
                            }
                        } catch (Exception ignored) {}
                    }
                    convMap.put("lastMessage", msg.getContent());
                    convMap.put("lastMessageTime", msg.getCreateTime());
                    int unreadCount = 0;
                    try {
                        unreadCount = messageMapper != null ? messageMapper.selectLetterUnreadCount(-9999L, conversationId) : 0;
                    } catch (Exception ignored) {}
                    convMap.put("unreadCount", unreadCount);
                    conversationMap.put(conversationId, convMap);
                }
            }
            List<Map<String, Object>> conversationList = new ArrayList<>(conversationMap.values());
            conversationList.sort((a, b) -> {
                Date timeA = (Date) a.get("lastMessageTime");
                Date timeB = (Date) b.get("lastMessageTime");
                if (timeA == null && timeB == null) return 0;
                if (timeA == null) return 1;
                if (timeB == null) return -1;
                return timeB.compareTo(timeA);
            });
            return Result.success(conversationList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取平台客服会话列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    @ApiOperation("获取会话列表")
    public Result<List<Map<String, Object>>> getConversationList(@RequestParam Long userId) {
        try {
            // 获取会话列表
            List<Message> conversations = messageService.findConversations(userId, 0, 100);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> conversationList = new ArrayList<>();
            for (Message msg : conversations) {
                if (msg == null || msg.getConversationId() == null) {
                    continue;
                }
                
                Map<String, Object> convMap = new HashMap<>();
                convMap.put("conversationId", msg.getConversationId());
                
                // 检查是否是平台客服消息（platform_，宠物之家客服）
                if (msg.getConversationId() != null && msg.getConversationId().startsWith("platform_")) {
                    convMap.put("isService", true);
                    convMap.put("isPlatform", true);
                    convMap.put("storeId", 0L);
                    convMap.put("otherUserId", "platform_0");
                    convMap.put("otherUserName", "宠物之家客服");
                    // 平台客服统一使用品牌图
                    String platformAvatar = "/static/images/宠物之家.png";
                    convMap.put("otherUserAvatar", platformAvatar);
                    convMap.put("storeName", "宠物之家客服");
                    convMap.put("storeAvatar", platformAvatar);
                }
                // 检查是否是门店客服消息（conversationId以"outlet_"开头）
                else if (msg.getConversationId() != null && msg.getConversationId().startsWith("outlet_")) {
                    convMap.put("isService", true);
                    convMap.put("isOutlet", true);
                    String outletStr = msg.getConversationId().replace("outlet_", "");
                    String[] parts = outletStr.split("_");
                    if (parts.length >= 1) {
                        try {
                            Long serviceStoreId = Long.parseLong(parts[0]);
                            convMap.put("storeId", serviceStoreId);
                            convMap.put("otherUserId", "outlet_" + serviceStoreId);
                            String storeName = "门店客服";
                            String storeAvatar = "/static/images/客服.png";
                            if (serviceStoreMapper != null) {
                                try {
                                    ServiceStore ss = serviceStoreMapper.selectById(serviceStoreId);
                                    if (ss != null) {
                                        storeName = (ss.getStoreName() != null && !ss.getStoreName().isEmpty() ? ss.getStoreName() : "门店") + "客服";
                                        // 小程序端显示 = 编辑服务门店里「门店头像(客服会话显示)」，未设置时回退管理员头像
                                        if (ss.getImageUrl() != null && !ss.getImageUrl().isEmpty()) {
                                            String img = ss.getImageUrl();
                                            storeAvatar = (img.startsWith("http://") || img.startsWith("https://")) ? img : (img.startsWith("/") ? img : "/" + img);
                                        } else if (adminService != null) {
                                            List<Admin> storeAdmins = adminService.listStaffByStoreId(serviceStoreId);
                                            storeAdmins.sort(Comparator.comparing(Admin::getId, Comparator.nullsLast(Comparator.naturalOrder())));
                                            for (Admin admin : storeAdmins) {
                                                if (admin.getAvatar() != null && !admin.getAvatar().isEmpty()) {
                                                    String a = admin.getAvatar();
                                                    storeAvatar = (a.startsWith("http://") || a.startsWith("https://")) ? a : (a.startsWith("/") ? a : "/" + a);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }
                            convMap.put("otherUserName", storeName);
                            convMap.put("otherUserAvatar", storeAvatar);
                            convMap.put("storeName", storeName);
                            convMap.put("storeAvatar", storeAvatar);
                        } catch (NumberFormatException e) {
                            convMap.put("otherUserId", msg.getConversationId());
                            convMap.put("otherUserName", "门店客服");
                            convMap.put("otherUserAvatar", "/static/images/客服.png");
                        }
                    }
                }
                // 检查是否是店铺客服消息（conversationId以"service_"开头）
                else if (msg.getConversationId() != null && msg.getConversationId().startsWith("service_")) {
                    convMap.put("isService", true);
                    String storeIdStr = msg.getConversationId().replace("service_", "");
                    String[] svcParts = storeIdStr.split("_");
                    try {
                        Long storeId = svcParts.length > 0 ? Long.parseLong(svcParts[0]) : Long.parseLong(storeIdStr);
                        convMap.put("storeId", storeId);
                        convMap.put("otherUserId", "service_" + storeId);
                        
                        // 获取店铺信息（名称和头像）
                        String storeName = "店铺客服"; // 默认名称
                        String storeAvatar = "/static/images/客服.png"; // 默认头像
                        if (storeMapper != null) {
                            try {
                                Store store = storeMapper.selectById(storeId);
                                if (store != null) {
                                    if (store.getName() != null && !store.getName().isEmpty()) {
                                        storeName = store.getName() + "客服";
                                    }
                                    if (store.getAvatar() != null && !store.getAvatar().isEmpty()) {
                                        storeAvatar = store.getAvatar();
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("获取店铺信息失败: " + e.getMessage());
                            }
                        }
                        convMap.put("otherUserName", storeName);
                        convMap.put("otherUserAvatar", storeAvatar);
                        convMap.put("storeName", storeName); // 同时设置storeName字段
                        convMap.put("storeAvatar", storeAvatar); // 同时设置storeAvatar字段
                    } catch (NumberFormatException e) {
                        convMap.put("otherUserId", msg.getConversationId());
                        convMap.put("otherUserName", "店铺客服");
                        convMap.put("otherUserAvatar", "/static/images/客服.png");
                    }
                } else {
                    // 普通用户消息
                    convMap.put("isService", false);
                    // 确定对方用户ID
                    Long otherUserId = null;
                    if (msg.getFromId() != null && msg.getFromId().equals(userId)) {
                        otherUserId = msg.getToId();
                    } else if (msg.getToId() != null && msg.getToId().equals(userId)) {
                        otherUserId = msg.getFromId();
                    } else {
                        // 如果都不匹配，使用非当前用户的ID
                        otherUserId = msg.getFromId() != null && !msg.getFromId().equals(userId) 
                            ? msg.getFromId() : msg.getToId();
                    }
                    convMap.put("otherUserId", otherUserId);
                }
                
                convMap.put("lastMessage", msg.getContent() != null ? msg.getContent() : "");
                convMap.put("lastMessageTime", msg.getCreateTime());
                
                // 获取未读消息数
                int unreadCount = 0;
                try {
                    unreadCount = messageService.findLetterUnreadCount(userId, msg.getConversationId());
                } catch (Exception e) {
                    // 如果查询未读数失败，设置为0
                    unreadCount = 0;
                }
                convMap.put("unreadCount", unreadCount);
                
                conversationList.add(convMap);
            }
            
            return Result.success(conversationList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 用户侧隐藏会话（与小程序「删除会话」一致：写入服务端，清缓存/换机后仍不展示）
     */
    @PostMapping("/conversations/hide")
    @ApiOperation("隐藏会话")
    public Result<Void> hideUserConversation(@RequestBody Map<String, Object> body) {
        try {
            if (body == null || body.get("userId") == null || body.get("conversationId") == null) {
                return Result.error("参数不完整");
            }
            Long userId = Long.valueOf(body.get("userId").toString());
            String conversationId = body.get("conversationId").toString().trim();
            if (conversationId.isEmpty()) {
                return Result.error("会话ID不能为空");
            }
            messageService.hideConversation(userId, conversationId);
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("隐藏会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送客服消息（店铺客服发送给用户）
     */
    @PostMapping("/service")
    @ApiOperation("发送客服消息")
    public Result<Map<String, Object>> sendServiceMessage(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long storeId = Long.valueOf(request.get("storeId").toString());
            String content = request.get("content").toString();
            
            if (userId == null || storeId == null || content == null || content.trim().isEmpty()) {
                return Result.error("参数不完整");
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发送客服消息失败: " + e.getMessage());
        }
    }

    /**
     * 发送门店客服消息（服务门店客服发送给用户）
     */
    @PostMapping("/outlet")
    @ApiOperation("发送门店客服消息")
    public Result<Map<String, Object>> sendOutletMessage(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long serviceStoreId = Long.valueOf(request.get("serviceStoreId").toString());
            String content = request.get("content").toString();

            if (userId == null || serviceStoreId == null || content == null || content.trim().isEmpty()) {
                return Result.error("参数不完整");
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发送门店客服消息失败: " + e.getMessage());
        }
    }

    /**
     * 发送平台客服消息（宠物之家客服发送给用户）
     */
    @PostMapping("/platform")
    @ApiOperation("发送平台客服消息")
    public Result<Map<String, Object>> sendPlatformMessage(@RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发送平台客服消息失败: " + e.getMessage());
        }
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
    @ApiOperation("获取点赞和收藏列表")
    public Result<Map<String, Object>> getLikesAndCollections(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long lastSeenAt) {
        try {
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
                e.printStackTrace();
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
            e.printStackTrace();
            return Result.error("获取点赞和收藏列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取新增关注列表
     */
    @GetMapping("/follows")
    @ApiOperation("获取新增关注列表")
    public Result<Map<String, Object>> getNewFollows(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long lastSeenAt) {
        try {
            if (userId == null) {
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
            wrapper.eq("following_id", userId);
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
                boolean isFollowed = userFollowService.isFollowing(userId, follow.getFollowerId());
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取新增关注列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评论与「@」相关通知。
     * 当前仅聚合帖子下的评论；站内 @ 提及若未单独落库，则不会出现在本接口中（与产品说明一致）。
     */
    @GetMapping("/comments")
    @ApiOperation("获取评论和@列表")
    public Result<Map<String, Object>> getCommentsAndMentions(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long lastSeenAt) {
        try {
            if (userId == null) {
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
            postWrapper.eq("user_id", userId);
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
                if (userId.equals(comment.getUserId())) continue;
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评论和@列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取推荐用户
     */
    @GetMapping("/recommended-users")
    @ApiOperation("获取推荐用户")
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
            
            // 从数据库查询可推荐用户：状态正常、排除自己和已关注的，按 id 倒序取 limit 个
            List<com.pethome.entity.User> users = new ArrayList<>();
            if (userMapper != null && !followingIds.isEmpty()) {
                QueryWrapper<com.pethome.entity.User> q = new QueryWrapper<>();
                q.eq("status", 1)
                 .ne("id", userId)
                 .notIn("id", followingIds)
                 .orderByDesc("id")
                 .last("LIMIT " + Math.min(Math.max(limit != null ? limit : 10, 1), 50));
                users = userMapper.selectList(q);
            } else if (userMapper != null) {
                QueryWrapper<com.pethome.entity.User> q = new QueryWrapper<>();
                q.eq("status", 1).ne("id", userId)
                 .orderByDesc("id")
                 .last("LIMIT " + Math.min(Math.max(limit != null ? limit : 10, 1), 50));
                users = userMapper.selectList(q);
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
            e.printStackTrace();
            return Result.error("获取推荐用户失败: " + e.getMessage());
        }
    }
}
