package com.pethome.service.community.support;

import com.pethome.entity.Admin;
import com.pethome.entity.ServiceStore;
import com.pethome.entity.Store;
import com.pethome.entity.User;
import com.pethome.service.admin.AdminService;
import com.pethome.service.appointment.ServiceStoreService;
import com.pethome.service.community.IMessageService;
import com.pethome.service.order.StoreService;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 会话列表批量查询组件。
 * <p>
 * 负责会话视图所需的店铺/门店/用户信息与未读数的批量补全，以及按最后消息时间排序，
 * 使主服务只关注会话解析与字段装配，避免 N+1 查询逻辑散落在聚合流程中。
 */
@Service
public class ConversationQuerySupport {

    private static final Logger log = LoggerFactory.getLogger(ConversationQuerySupport.class);

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

    /**
     * 批量加载店铺信息（一条 IN 查询，替代逐会话查询的 N+1）。
     */
    public Map<Long, Store> batchLoadStores(Set<Long> storeIds) {
        Map<Long, Store> storeMap = new HashMap<>();
        if (storeService == null || storeIds == null || storeIds.isEmpty()) {
            return storeMap;
        }
        try {
            for (Store store : storeService.listByIds(storeIds)) {
                if (store != null && store.getId() != null) {
                    storeMap.put(store.getId(), store);
                }
            }
        } catch (Exception e) {
            log.warn("批量获取店铺信息失败", e);
        }
        return storeMap;
    }

    /**
     * 批量加载服务门店信息（一条 IN 查询，替代逐会话查询的 N+1）。
     */
    public Map<Long, ServiceStore> batchLoadServiceStores(Set<Long> serviceStoreIds) {
        Map<Long, ServiceStore> serviceStoreMap = new HashMap<>();
        if (serviceStoreService == null || serviceStoreIds == null || serviceStoreIds.isEmpty()) {
            return serviceStoreMap;
        }
        try {
            for (ServiceStore ss : serviceStoreService.getStoresByIds(serviceStoreIds)) {
                if (ss != null && ss.getId() != null) {
                    serviceStoreMap.put(ss.getId(), ss);
                }
            }
        } catch (Exception ignored) {
        }
        return serviceStoreMap;
    }

    /**
     * 无头像门店的回退头像：一条 IN 查询批量获取各门店员工头像（取 id 最小的员工头像，与单查版行为一致）。
     */
    public Map<Long, String> resolveFallbackStoreAvatars(Map<Long, ServiceStore> serviceStoreMap) {
        Map<Long, String> fallbackAvatarByStore = new HashMap<>();
        if (serviceStoreMap == null || serviceStoreMap.isEmpty() || adminService == null) {
            return fallbackAvatarByStore;
        }
        Set<Long> noImageStoreIds = new HashSet<>();
        for (ServiceStore ss : serviceStoreMap.values()) {
            if (ss.getImageUrl() == null || ss.getImageUrl().isEmpty()) {
                noImageStoreIds.add(ss.getId());
            }
        }
        if (noImageStoreIds.isEmpty()) {
            return fallbackAvatarByStore;
        }
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
        } catch (Exception ignored) {
        }
        return fallbackAvatarByStore;
    }

    /**
     * 批量加载用户信息（一条 IN 查询，替代逐会话 getUserById 的 N+1）。
     */
    public Map<Long, User> batchLoadUsers(Set<Long> userIds) {
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
     * 一条 GROUP BY SQL 替代逐会话 selectLetterUnreadCount 的 N+1。
     */
    public Map<String, Map<Long, Integer>> batchUnreadCounts(List<String> conversationIds) {
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

    /**
     * 按最后消息时间降序排序（无时间值的会话排在最后）。
     */
    public List<Map<String, Object>> sortByLastMessageTimeDesc(Map<String, Map<String, Object>> conversationMap) {
        List<Map<String, Object>> conversationList = new ArrayList<>(conversationMap.values());
        conversationList.sort((a, b) -> {
            Date timeA = (Date) a.get("lastMessageTime");
            Date timeB = (Date) b.get("lastMessageTime");
            if (timeA == null && timeB == null) {
                return 0;
            }
            if (timeA == null) {
                return 1;
            }
            if (timeB == null) {
                return -1;
            }
            return timeB.compareTo(timeA); // 降序，最新的在前
        });
        return conversationList;
    }
}
