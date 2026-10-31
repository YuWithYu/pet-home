package com.pethome.service.community.impl;

import com.pethome.common.BusinessException;
import com.pethome.entity.Admin;
import com.pethome.entity.Message;
import com.pethome.entity.ServiceStore;
import com.pethome.entity.Store;
import com.pethome.security.AdminContext;
import com.pethome.service.admin.AdminService;
import com.pethome.service.appointment.ServiceStoreService;
import com.pethome.service.community.IMessageService;
import com.pethome.service.community.MessageAppService;
import com.pethome.service.community.MessageConversationService;
import com.pethome.service.order.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信应用服务实现：原 MessageController 的私信/客服消息与会话聚合逻辑整体下沉至此。
 */
@Service
public class MessageAppServiceImpl implements MessageAppService {

    private static final Logger log = LoggerFactory.getLogger(MessageAppServiceImpl.class);

    @Autowired
    private IMessageService messageService;

    @Autowired
    private MessageConversationService messageConversationService;

    @Autowired(required = false)
    private StoreService storeService;

    @Autowired(required = false)
    private ServiceStoreService serviceStoreService;

    @Autowired(required = false)
    private AdminService adminService;

    @Autowired(required = false)
    private AdminContext adminContext;

    @Override
    public Map<String, Object> getPrivateMessages(Long userId, Long targetUserId, String conversationId,
                                                  Integer page, Integer size, boolean staffOrAdmin, Long loginUserId) {
        // 安全修复保留：非管理端强制 userId=登录态，且 conversationId 必须包含登录用户（客服后台 staff/admin 放行）。
        if (!staffOrAdmin) {
            if (loginUserId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            userId = loginUserId;
            if (conversationId != null && !conversationId.trim().isEmpty()
                    && !MessageConversationGuard.owns(conversationId.trim(), loginUserId)) {
                throw new BusinessException(403, "无权查看此会话");
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
            throw new BusinessException("参数不完整：需要提供targetUserId或conversationId");
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
        return result;
    }

    @Override
    public Map<String, Object> sendPrivateMessage(Map<String, Object> request, boolean staffOrAdmin, Long loginUserId) {
        Long senderId = Long.valueOf(request.get("senderId").toString());
        Long receiverId = Long.valueOf(request.get("receiverId").toString());
        String content = request.get("content").toString();
        String conversationId = request.get("conversationId") != null ? request.get("conversationId").toString() : null;

        // 安全修复保留：非管理端强制 senderId=登录态，且所传 conversationId 必须归属本人（客服后台 staff/admin 放行）。
        if (!staffOrAdmin) {
            if (loginUserId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            senderId = loginUserId;
            if (conversationId != null && !conversationId.trim().isEmpty()
                    && !MessageConversationGuard.owns(conversationId.trim(), loginUserId)) {
                throw new BusinessException(403, "无权在此会话发送消息");
            }
        }

        if (senderId == null || receiverId == null) {
            throw new BusinessException("参数不完整");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("消息内容不能为空");
        }

        // 生成会话ID（如果未提供）
        String finalConversationId;
        if (conversationId != null && !conversationId.trim().isEmpty()) {
            finalConversationId = conversationId;
        } else {
            finalConversationId = generateConversationId(senderId, receiverId);
        }

        Message message = new Message();
        message.setFromId(senderId);
        message.setToId(receiverId);
        message.setConversationId(finalConversationId);
        message.setContent(content);
        message.setStatus(0); // 0表示未读
        message.setCreateTime(new Date());

        int result = messageService.addMessage(message);
        if (result <= 0) {
            throw new BusinessException("发送失败");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", message.getId());
        response.put("senderId", message.getFromId());
        response.put("receiverId", message.getToId());
        response.put("content", message.getContent());
        response.put("createTime", message.getCreateTime());
        return response;
    }

    @Override
    public List<Map<String, Object>> getAllServiceConversations(Long storeId, String token) {
        Admin admin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
        // 安全修复：非管理端 token 不得回退到前端传入的 storeId，否则任意用户可读取指定店铺的全部客服会话
        if (admin == null) {
            throw new BusinessException(401, "请先登录管理端");
        }
        if (!isPlatformAdmin(admin)) {
            // 店铺管理员只能查看本店铺会话；未绑定店铺时返回空列表，避免退化为全平台会话
            if (admin.getStoreId() == null) {
                return new ArrayList<>();
            }
            return messageConversationService.getAllServiceConversations(admin.getStoreId());
        }
        return messageConversationService.getAllServiceConversations(storeId);
    }

    @Override
    public List<Map<String, Object>> getAllOutletConversations(Long serviceStoreId, String token) {
        Admin admin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
        // 安全修复：非管理端 token 不得回退到前端传入的 serviceStoreId
        if (admin == null) {
            throw new BusinessException(401, "请先登录管理端");
        }
        if (!isPlatformAdmin(admin)) {
            if (admin.getServiceStoreId() == null) {
                return new ArrayList<>();
            }
            return messageConversationService.getAllOutletConversations(admin.getServiceStoreId());
        }
        return messageConversationService.getAllOutletConversations(serviceStoreId);
    }

    /** 角色归一：super_admin 与 admin 均视为平台管理员 */
    private boolean isPlatformAdmin(Admin admin) {
        String role = admin == null || admin.getRole() == null ? "" : admin.getRole();
        return "admin".equals(role) || "super_admin".equals(role);
    }

    @Override
    public List<Map<String, Object>> getAllPlatformConversations(String token) {
        Admin admin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
        if (admin == null) {
            throw new BusinessException("请先登录");
        }
        return messageConversationService.getAllPlatformConversations();
    }

    @Override
    public List<Map<String, Object>> getConversationList(Long userId, boolean staffOrAdmin, Long loginUserId) {
        // 安全修复保留：会话列表属个人数据，非管理端强制以登录态为准（防止枚举他人会话列表）
        if (!staffOrAdmin) {
            if (loginUserId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            userId = loginUserId;
        }
        return messageConversationService.getUserConversationList(userId);
    }

    @Override
    public void hideUserConversation(Map<String, Object> body, boolean staffOrAdmin, Long loginUserId) {
        if (body == null || body.get("userId") == null || body.get("conversationId") == null) {
            throw new BusinessException("参数不完整");
        }
        Long userId = Long.valueOf(body.get("userId").toString());
        String conversationId = body.get("conversationId").toString().trim();

        // 安全修复保留：隐藏操作以登录态为准，且会话必须归属本人（防止隐藏他人会话）
        if (!staffOrAdmin) {
            if (loginUserId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            userId = loginUserId;
            if (!MessageConversationGuard.owns(conversationId, loginUserId)) {
                throw new BusinessException(403, "无权操作此会话");
            }
        }
        if (conversationId.isEmpty()) {
            throw new BusinessException("会话ID不能为空");
        }
        messageService.hideConversation(userId, conversationId);
    }

    @Override
    public Map<String, Object> sendServiceMessage(Map<String, Object> request, String token, boolean adminSide) {
        // 安全修复保留：此前任意登录用户即可指定 storeId 冒充店铺客服给任意用户发私信
        if (!adminSide) {
            throw new BusinessException(403, "无权发送客服消息");
        }
        Long ownStoreId = adminContext != null ? adminContext.getCurrentStoreId(token) : null;
        boolean platformAdmin = adminContext != null && adminContext.isPlatformAdmin(token);

        Long userId = Long.valueOf(request.get("userId").toString());
        Long storeId = Long.valueOf(request.get("storeId").toString());
        String content = request.get("content").toString();

        if (userId == null || storeId == null || content == null || content.trim().isEmpty()) {
            throw new BusinessException("参数不完整");
        }
        // 非平台管理员只能以自己店铺身份发消息，防止冒充其他店铺客服
        if (!platformAdmin && (ownStoreId == null || !ownStoreId.equals(storeId))) {
            throw new BusinessException(403, "无权以该店铺身份发送消息");
        }
        if (content == null) {
            throw new BusinessException("消息内容不能为空");
        }

        // 生成客服会话ID（格式：service_storeId_userId）
        String conversationId = "service_" + storeId + "_" + userId;

        // 创建消息（fromId使用一个特殊的客服ID，比如-storeId，表示这是店铺客服）
        Message message = new Message();
        message.setFromId(-storeId);
        message.setToId(userId);
        message.setConversationId(conversationId);
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());

        int result = messageService.addMessage(message);
        if (result <= 0) {
            throw new BusinessException("发送失败");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", message.getId());
        response.put("senderId", message.getFromId());
        response.put("receiverId", message.getToId());
        response.put("content", message.getContent());
        response.put("createTime", message.getCreateTime());
        response.put("isService", true);
        return response;
    }

    @Override
    public Map<String, Object> sendOutletMessage(Map<String, Object> request, String token, boolean staffOrAdmin) {
        // 安全修复保留：此前任意登录用户即可指定 serviceStoreId 冒充门店客服给任意用户发私信
        if (!staffOrAdmin) {
            throw new BusinessException(403, "无权发送门店客服消息");
        }
        Long ownServiceStoreId = adminContext != null ? adminContext.getCurrentServiceStoreId(token) : null;
        boolean platformAdmin = adminContext != null && adminContext.isPlatformAdmin(token);

        Long userId = Long.valueOf(request.get("userId").toString());
        Long serviceStoreId = Long.valueOf(request.get("serviceStoreId").toString());
        String content = request.get("content").toString();

        if (userId == null || serviceStoreId == null || content == null || content.trim().isEmpty()) {
            throw new BusinessException("参数不完整");
        }
        // 非平台管理员只能以自己所属服务门店身份发消息
        if (!platformAdmin && (ownServiceStoreId == null || !ownServiceStoreId.equals(serviceStoreId))) {
            throw new BusinessException(403, "无权以该门店身份发送消息");
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
        if (result <= 0) {
            throw new BusinessException("发送失败");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", message.getId());
        response.put("senderId", message.getFromId());
        response.put("receiverId", message.getToId());
        response.put("content", message.getContent());
        response.put("createTime", message.getCreateTime());
        response.put("isService", true);
        response.put("isOutlet", true);
        return response;
    }

    @Override
    public Map<String, Object> sendPlatformMessage(Map<String, Object> request, String token) {
        Admin admin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
        if (admin == null) {
            throw new BusinessException("请先登录");
        }
        Long userId = Long.valueOf(request.get("userId").toString());
        String content = request.get("content").toString();
        if (userId == null || content == null || content.trim().isEmpty()) {
            throw new BusinessException("参数不完整");
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
        if (result <= 0) {
            throw new BusinessException("发送失败");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", message.getId());
        response.put("senderId", message.getFromId());
        response.put("receiverId", message.getToId());
        response.put("content", message.getContent());
        response.put("createTime", message.getCreateTime());
        response.put("isService", true);
        response.put("isPlatform", true);
        return response;
    }

    /** 生成会话ID（确保两个用户之间的会话ID一致） */
    private String generateConversationId(Long userId1, Long userId2) {
        if (userId1 < userId2) {
            return userId1 + "_" + userId2;
        }
        return userId2 + "_" + userId1;
    }

    /** 格式化时间 */
    private String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(date);
    }
}
