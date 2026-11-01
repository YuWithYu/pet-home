package com.pethome.service.community.support;

import com.pethome.entity.Admin;
import com.pethome.entity.Message;
import com.pethome.entity.ServiceStore;
import com.pethome.entity.Store;
import com.pethome.service.admin.AdminService;
import com.pethome.service.appointment.ServiceStoreService;
import com.pethome.service.community.IMessageService;
import com.pethome.service.order.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户侧会话视图装配组件。
 * <p>
 * 将用户会话列表转换为前端展示结构：识别平台/门店/店铺客服会话，补齐对端名称头像与未读数。
 */
@Service
public class UserConversationViewAssembler {

    private static final Logger log = LoggerFactory.getLogger(UserConversationViewAssembler.class);

    @Autowired
    private IMessageService messageService;

    @Autowired(required = false)
    private StoreService storeService;

    @Autowired(required = false)
    private ServiceStoreService serviceStoreService;

    @Autowired(required = false)
    private AdminService adminService;

    /**
     * 构建指定用户的会话列表视图。
     */
    public List<Map<String, Object>> buildUserConversationList(Long userId) {
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
                            } catch (Exception ignored) {
                            }
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
}
