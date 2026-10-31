package com.pethome.service.community.impl;

import com.pethome.entity.Admin;
import com.pethome.entity.Message;
import com.pethome.entity.ServiceStore;
import com.pethome.entity.Store;
import com.pethome.entity.User;
import com.pethome.service.admin.AdminService;
import com.pethome.service.appointment.ServiceStoreService;
import com.pethome.service.community.IMessageService;
import com.pethome.service.community.MessageConversationService;
import com.pethome.service.order.StoreService;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 消息会话聚合实现：Controller 只做参数解析与结果包装，会话组装与信息补全集中在此。
 */
@Service
public class MessageConversationServiceImpl implements MessageConversationService {

    private static final Logger log = LoggerFactory.getLogger(MessageConversationServiceImpl.class);

    @Autowired
    private IMessageService messageService;

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private StoreService storeService;

    @Autowired(required = false)
    private ServiceStoreService serviceStoreService;

    @Autowired(required = false)
    private AdminService adminService;

    @Override
    public List<Map<String, Object>> getAllServiceConversations(Long storeId) {
        List<Message> allMessages;
        if (messageService == null) {
            // 服务不可用时保持原有空列表语义
            return new ArrayList<>();
        }
        if (storeId != null) {
            allMessages = messageService.findServiceConversationsByStoreId(storeId);
        } else {
            allMessages = messageService.findAllServiceConversations();
        }

        // 用于存储唯一的会话
        Map<String, Map<String, Object>> conversationMap = new HashMap<>();
        Set<Long> storeIdSet = new HashSet<>();
        Set<Long> userIdSet = new HashSet<>();

        // 第一遍：仅解析会话（不查库），收集需要批量查询的店铺/用户ID
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
                convMap.put("lastMessage", msg.getContent());
                convMap.put("lastMessageTime", msg.getCreateTime());

                storeIdSet.add(msgStoreId);
                if (userId != null) {
                    userIdSet.add(userId);
                }
                conversationMap.put(conversationId, convMap);
            }
        }

        // 批量查询：店铺/用户/未读数各一条 SQL（替代逐会话循环查询的 N+1）
        Map<Long, Store> storeMap = new HashMap<>();
        if (storeService != null && !storeIdSet.isEmpty()) {
            try {
                for (Store store : storeService.listByIds(storeIdSet)) {
                    if (store != null && store.getId() != null) {
                        storeMap.put(store.getId(), store);
                    }
                }
            } catch (Exception e) {
                log.warn("批量获取店铺信息失败", e);
            }
        }
        Map<Long, User> userMap = batchLoadUsers(userIdSet);
        Map<String, Map<Long, Integer>> unreadMap = batchUnreadCounts(new ArrayList<>(conversationMap.keySet()));

        // 第二遍：从批量结果填充展示信息
        for (Map<String, Object> convMap : conversationMap.values()) {
            Long msgStoreId = (Long) convMap.get("storeId");
            Long userId = (Long) convMap.get("userId");

            // 获取店铺信息
            String storeName = "店铺客服";
            String storeAvatar = "/static/images/customer-service.svg";
            Store store = msgStoreId != null ? storeMap.get(msgStoreId) : null;
            if (store != null) {
                if (store.getName() != null && !store.getName().isEmpty()) {
                    storeName = store.getName() + "客服";
                }
                if (store.getAvatar() != null && !store.getAvatar().isEmpty()) {
                    storeAvatar = store.getAvatar();
                }
            }
            convMap.put("otherUserName", storeName);
            convMap.put("storeName", storeName);
            convMap.put("otherUserAvatar", storeAvatar);
            convMap.put("storeAvatar", storeAvatar);

            // 获取用户信息（会话列表中应该显示用户信息，而不是店铺信息）
            User user = userId != null ? userMap.get(userId) : null;
            if (user != null) {
                convMap.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                convMap.put("userNickname", user.getNickname());
                convMap.put("userAvatar", user.getAvatar());
            }

            // 客服未读的消息，即用户发送给客服的未读消息：toId = -storeId 且 status = 0
            int unreadCount = 0;
            Map<Long, Integer> byToId = unreadMap.get((String) convMap.get("conversationId"));
            if (byToId != null && msgStoreId != null) {
                unreadCount = byToId.getOrDefault(-msgStoreId, 0);
            }
            convMap.put("unreadCount", unreadCount);
        }

        return sortByLastMessageTimeDesc(conversationMap);
    }

    @Override
    public List<Map<String, Object>> getAllOutletConversations(Long serviceStoreId) {
        List<Message> allMessages = new ArrayList<>();
        if (messageService != null) {
            if (serviceStoreId != null) {
                allMessages = messageService.findOutletConversationsByServiceStoreId(serviceStoreId);
            } else {
                allMessages = messageService.findAllOutletConversations();
            }
        }

        Map<String, Map<String, Object>> conversationMap = new HashMap<>();
        Set<Long> serviceStoreIdSet = new HashSet<>();
        Set<Long> userIdSet = new HashSet<>();

        // 第一遍：仅解析会话（不查库），收集需要批量查询的门店/用户ID
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
                convMap.put("lastMessage", msg.getContent());
                convMap.put("lastMessageTime", msg.getCreateTime());

                serviceStoreIdSet.add(msgServiceStoreId);
                if (userId != null) {
                    userIdSet.add(userId);
                }
                conversationMap.put(conversationId, convMap);
            }
        }

        // 批量查询：服务门店/用户/未读数各一条 SQL（替代逐会话循环查询的 N+1）
        Map<Long, ServiceStore> serviceStoreMap = new HashMap<>();
        if (serviceStoreService != null && !serviceStoreIdSet.isEmpty()) {
            try {
                for (ServiceStore ss : serviceStoreService.getStoresByIds(serviceStoreIdSet)) {
                    if (ss != null && ss.getId() != null) {
                        serviceStoreMap.put(ss.getId(), ss);
                    }
                }
            } catch (Exception ignored) {}
        }
        Map<Long, User> userMap = batchLoadUsers(userIdSet);
        Map<String, Map<Long, Integer>> unreadMap = batchUnreadCounts(new ArrayList<>(conversationMap.keySet()));

        // 无头像门店的回退：一条 IN 查询批量获取各门店员工头像（取 id 最小的员工头像，与单查版行为一致）
        Map<Long, String> fallbackAvatarByStore = new HashMap<>();
        Set<Long> noImageStoreIds = new HashSet<>();
        for (ServiceStore ss : serviceStoreMap.values()) {
            if (ss.getImageUrl() == null || ss.getImageUrl().isEmpty()) {
                noImageStoreIds.add(ss.getId());
            }
        }
        if (!noImageStoreIds.isEmpty() && adminService != null) {
            try {
                for (Admin admin : adminService.listStaffByStoreIds(noImageStoreIds)) {
                    Long storeId = admin.getServiceStoreId();
                    if (storeId != null && admin.getAvatar() != null && !admin.getAvatar().isEmpty()
                            && !fallbackAvatarByStore.containsKey(storeId)) {
                        String a = admin.getAvatar();
                        fallbackAvatarByStore.put(storeId,
                                (a.startsWith("http://") || a.startsWith("https://")) ? a : (a.startsWith("/") ? a : "/" + a));
                    }
                }
            } catch (Exception ignored) {}
        }

        // 第二遍：从批量结果填充展示信息
        for (Map<String, Object> convMap : conversationMap.values()) {
            Long msgServiceStoreId = (Long) convMap.get("serviceStoreId");
            Long userId = (Long) convMap.get("userId");

            String storeName = "门店客服";
            String storeAvatar = "/static/images/customer-service.svg";
            ServiceStore ss = msgServiceStoreId != null ? serviceStoreMap.get(msgServiceStoreId) : null;
            if (ss != null) {
                storeName = (ss.getStoreName() != null && !ss.getStoreName().isEmpty() ? ss.getStoreName() : "门店") + "客服";
                // 小程序端显示 = 编辑服务门店里「门店头像(客服会话显示)」，未设置时回退管理员头像
                if (ss.getImageUrl() != null && !ss.getImageUrl().isEmpty()) {
                    String img = ss.getImageUrl();
                    storeAvatar = (img.startsWith("http://") || img.startsWith("https://")) ? img : (img.startsWith("/") ? img : "/" + img);
                } else if (msgServiceStoreId != null) {
                    String fallback = fallbackAvatarByStore.get(msgServiceStoreId);
                    if (fallback != null) {
                        storeAvatar = fallback;
                    }
                }
            }
            convMap.put("storeName", storeName);
            convMap.put("otherUserName", storeName);
            convMap.put("storeAvatar", storeAvatar);
            convMap.put("otherUserAvatar", storeAvatar);

            User user = userId != null ? userMap.get(userId) : null;
            if (user != null) {
                convMap.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                convMap.put("userNickname", user.getNickname());
                convMap.put("userAvatar", user.getAvatar());
            }

            int unreadCount = 0;
            Map<Long, Integer> byToId = unreadMap.get((String) convMap.get("conversationId"));
            if (byToId != null && msgServiceStoreId != null) {
                unreadCount = byToId.getOrDefault(-msgServiceStoreId, 0);
            }
            convMap.put("unreadCount", unreadCount);
        }

        return sortByLastMessageTimeDesc(conversationMap);
    }

    @Override
    public List<Map<String, Object>> getAllPlatformConversations() {
        List<Message> allMessages = messageService != null ? messageService.findAllPlatformConversations() : new ArrayList<>();
        Map<String, Map<String, Object>> conversationMap = new HashMap<>();
        Set<Long> userIdSet = new HashSet<>();

        // 第一遍：仅解析会话（不查库），收集需要批量查询的用户ID
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
                convMap.put("lastMessage", msg.getContent());
                convMap.put("lastMessageTime", msg.getCreateTime());
                if (userId != null) {
                    userIdSet.add(userId);
                }
                conversationMap.put(conversationId, convMap);
            }
        }

        // 批量查询：用户/未读数各一条 SQL（替代逐会话循环查询的 N+1）
        Map<Long, User> userMap = batchLoadUsers(userIdSet);
        Map<String, Map<Long, Integer>> unreadMap = batchUnreadCounts(new ArrayList<>(conversationMap.keySet()));

        // 第二遍：从批量结果填充展示信息
        for (Map<String, Object> convMap : conversationMap.values()) {
            Long userId = (Long) convMap.get("userId");
            User user = userId != null ? userMap.get(userId) : null;
            if (user != null) {
                convMap.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                convMap.put("userNickname", user.getNickname());
                convMap.put("userAvatar", user.getAvatar());
            }
            // 平台客服的接收方固定为 -9999
            int unreadCount = 0;
            Map<Long, Integer> byToId = unreadMap.get((String) convMap.get("conversationId"));
            if (byToId != null) {
                unreadCount = byToId.getOrDefault(-9999L, 0);
            }
            convMap.put("unreadCount", unreadCount);
        }

        return sortByLastMessageTimeDesc(conversationMap);
    }

    @Override
    public List<Map<String, Object>> getUserConversationList(Long userId) {
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
            if (msg.getConversationId().startsWith("platform_")) {
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
            else if (msg.getConversationId().startsWith("outlet_")) {
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
                        if (serviceStoreService != null) {
                            try {
                                ServiceStore ss = serviceStoreService.getStoreById(serviceStoreId);
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
            else if (msg.getConversationId().startsWith("service_")) {
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
                    if (storeService != null) {
                        try {
                            Store store = storeService.getById(storeId);
                            if (store != null) {
                                if (store.getName() != null && !store.getName().isEmpty()) {
                                    storeName = store.getName() + "客服";
                                }
                                if (store.getAvatar() != null && !store.getAvatar().isEmpty()) {
                                    storeAvatar = store.getAvatar();
                                }
                            }
                        } catch (Exception e) {
                            log.warn("获取店铺信息失败", e);
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
                Long otherUserId;
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
            int unreadCount;
            try {
                unreadCount = messageService.findLetterUnreadCount(userId, msg.getConversationId());
            } catch (Exception e) {
                // 如果查询未读数失败，设置为0
                unreadCount = 0;
            }
            convMap.put("unreadCount", unreadCount);

            conversationList.add(convMap);
        }

        return conversationList;
    }

    private List<Map<String, Object>> sortByLastMessageTimeDesc(Map<String, Map<String, Object>> conversationMap) {
        List<Map<String, Object>> conversationList = new ArrayList<>(conversationMap.values());
        conversationList.sort((a, b) -> {
            Date timeA = (Date) a.get("lastMessageTime");
            Date timeB = (Date) b.get("lastMessageTime");
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA); // 降序，最新的在前
        });
        return conversationList;
    }

    /**
     * 批量加载用户信息（一条 IN 查询，替代逐会话 getUserById 的 N+1）
     */
    private Map<Long, User> batchLoadUsers(Set<Long> userIds) {
        Map<Long, User> userMap = new HashMap<>();
        if (userService == null || userIds == null || userIds.isEmpty()) {
            return userMap;
        }
        try {
            for (User user : userService.getUsersByIds(userIds)) {
                if (user != null && user.getId() != null) {
                    userMap.put(user.getId(), user);
                }
            }
        } catch (Exception e) {
            log.warn("批量加载用户信息失败", e);
        }
        return userMap;
    }

    /**
     * 批量查询会话未读数：conversationId -> (toId -> 未读数)，
     * 一条 GROUP BY SQL 替代逐会话 selectLetterUnreadCount 的 N+1
     */
    private Map<String, Map<Long, Integer>> batchUnreadCounts(List<String> conversationIds) {
        Map<String, Map<Long, Integer>> result = new HashMap<>();
        if (messageService == null || conversationIds == null || conversationIds.isEmpty()) {
            return result;
        }
        try {
            List<Map<String, Object>> rows = messageService.findUnreadCounts(conversationIds);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    Object cid = row.get("conversationId");
                    Object toId = row.get("toId");
                    Object cnt = row.get("cnt");
                    if (cid != null && toId instanceof Number && cnt instanceof Number) {
                        result.computeIfAbsent(cid.toString(), k -> new HashMap<>())
                                .put(((Number) toId).longValue(), ((Number) cnt).intValue());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("批量查询会话未读数失败", e);
        }
        return result;
    }
}
