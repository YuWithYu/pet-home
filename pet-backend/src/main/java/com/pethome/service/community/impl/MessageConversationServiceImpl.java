package com.pethome.service.community.impl;

import com.pethome.entity.Message;
import com.pethome.entity.ServiceStore;
import com.pethome.entity.Store;
import com.pethome.entity.User;
import com.pethome.service.community.IMessageService;
import com.pethome.service.community.MessageConversationService;
import com.pethome.service.community.support.ConversationQuerySupport;
import com.pethome.service.community.support.UserConversationViewAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 消息会话聚合实现：Controller 只做参数解析与结果包装，会话组装与信息补全集中在此。
 * 批量查询与用户侧视图装配下沉至 support 组件。
 */
@Service
public class MessageConversationServiceImpl implements MessageConversationService {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private ConversationQuerySupport conversationQuerySupport;

    @Autowired
    private UserConversationViewAssembler userConversationViewAssembler;

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
        Map<Long, Store> storeMap = conversationQuerySupport.batchLoadStores(storeIdSet);
        Map<Long, User> userMap = conversationQuerySupport.batchLoadUsers(userIdSet);
        Map<String, Map<Long, Integer>> unreadMap =
                conversationQuerySupport.batchUnreadCounts(new ArrayList<>(conversationMap.keySet()));

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

        return conversationQuerySupport.sortByLastMessageTimeDesc(conversationMap);
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
        Map<Long, ServiceStore> serviceStoreMap = conversationQuerySupport.batchLoadServiceStores(serviceStoreIdSet);
        Map<Long, User> userMap = conversationQuerySupport.batchLoadUsers(userIdSet);
        Map<String, Map<Long, Integer>> unreadMap =
                conversationQuerySupport.batchUnreadCounts(new ArrayList<>(conversationMap.keySet()));

        // 无头像门店的回退：一条 IN 查询批量获取各门店员工头像（取 id 最小的员工头像，与单查版行为一致）
        Map<Long, String> fallbackAvatarByStore =
                conversationQuerySupport.resolveFallbackStoreAvatars(serviceStoreMap);

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

        return conversationQuerySupport.sortByLastMessageTimeDesc(conversationMap);
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
        Map<Long, User> userMap = conversationQuerySupport.batchLoadUsers(userIdSet);
        Map<String, Map<Long, Integer>> unreadMap =
                conversationQuerySupport.batchUnreadCounts(new ArrayList<>(conversationMap.keySet()));

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

        return conversationQuerySupport.sortByLastMessageTimeDesc(conversationMap);
    }

    @Override
    public List<Map<String, Object>> getUserConversationList(Long userId) {
        return userConversationViewAssembler.buildUserConversationList(userId);
    }
}
