package com.pethome.service.community;

import java.util.List;
import java.util.Map;

/**
 * 消息会话聚合服务：客服/门店/平台会话列表与会话详情列表的组装（含店铺、门店、用户信息与未读数补全）。
 */
public interface MessageConversationService {

    /**
     * 店铺客服会话列表（service_ 前缀）。
     *
     * @param storeId 已按登录态收敛后的店铺 ID，可为空（平台视角查全部）
     */
    List<Map<String, Object>> getAllServiceConversations(Long storeId);

    /**
     * 门店客服会话列表（outlet_ 前缀）。
     *
     * @param serviceStoreId 已按登录态收敛后的门店 ID，可为空（平台视角查全部）
     */
    List<Map<String, Object>> getAllOutletConversations(Long serviceStoreId);

    /** 平台客服会话列表（platform_ 前缀）。 */
    List<Map<String, Object>> getAllPlatformConversations();

    /** 指定用户的会话列表（普通用户/管理端查看）。 */
    List<Map<String, Object>> getUserConversationList(Long userId);
}
