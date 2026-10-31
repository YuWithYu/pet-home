package com.pethome.service.community;

import java.util.List;
import java.util.Map;

/**
 * 私信应用服务：用户私信读写、客服/门店/平台会话消息发送与会话列表聚合。
 *
 * <p>原 MessageController 中的会话归属校验、店铺/门店信息组装、消息落库等逻辑整体下沉至此，
 * Controller 只负责解析登录态并包装 Result。</p>
 */
public interface MessageAppService {

    /**
     * 两个用户 / 客服会话的消息列表。
     *
     * @param userId         请求传入的 userId（非管理端会被登录态覆盖）
     * @param staffOrAdmin   当前请求是否来自客服后台（staff/admin）
     * @param loginUserId    拦截器写入的登录用户 ID，可为空
     */
    Map<String, Object> getPrivateMessages(Long userId, Long targetUserId, String conversationId,
                                           Integer page, Integer size, boolean staffOrAdmin, Long loginUserId);

    /** 发送私信 */
    Map<String, Object> sendPrivateMessage(Map<String, Object> request, boolean staffOrAdmin, Long loginUserId);

    /** 店铺客服会话列表（店铺管理员只看自己店铺） */
    List<Map<String, Object>> getAllServiceConversations(Long storeId, String token);

    /** 门店客服会话列表（门店管理员只看自己门店） */
    List<Map<String, Object>> getAllOutletConversations(Long serviceStoreId, String token);

    /** 平台客服会话列表（需平台管理员登录） */
    List<Map<String, Object>> getAllPlatformConversations(String token);

    /** 用户会话列表 */
    List<Map<String, Object>> getConversationList(Long userId, boolean staffOrAdmin, Long loginUserId);

    /** 用户侧隐藏会话 */
    void hideUserConversation(Map<String, Object> body, boolean staffOrAdmin, Long loginUserId);

    /** 店铺客服发送消息 */
    Map<String, Object> sendServiceMessage(Map<String, Object> request, String token, boolean adminSide);

    /** 门店客服发送消息 */
    Map<String, Object> sendOutletMessage(Map<String, Object> request, String token, boolean staffOrAdmin);

    /** 平台客服发送消息 */
    Map<String, Object> sendPlatformMessage(Map<String, Object> request, String token);
}
