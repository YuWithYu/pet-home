package com.pethome.mapper;

import com.pethome.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 私信/通知 Mapper（由旧 dao.MessageMapper 合并而来，XML 绑定见 resources/mapper/MessageMapper.xml）
 */
@Mapper
public interface MessageMapper {

    // 查询当前用户的会话数量.   分页用
    int selectConversationCount(@Param("userId") Long userId);

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    List<Message> selectConversations(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    // 查询某个会话所包含的私信数量.
    int selectLetterCount(@Param("conversationId") String conversationId);

    // 查询未读私信的数量   通过拼接 conversationId 决定是查询所有未读数量还是某条会话的未读数量
    int selectLetterUnreadCount(@Param("userId") Long userId, @Param("conversationId") String conversationId);

    /**
     * 批量查询多个会话的未读数（按 conversation_id + to_id 分组），
     * 替代逐会话循环调用 selectLetterUnreadCount 的 N+1 查询
     */
    List<Map<String, Object>> selectUnreadCounts(@Param("conversationIds") List<String> conversationIds);

    // 查询某个会话所包含的私信列表.    某条会话的详细信息
    List<Message> selectLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    int updateStatus(@Param("ids") List<Long> ids, @Param("status") int status);

    // 新增消息
    int insertMessage(Message message);

    // 查询某个主题下的最新通知
    Message selectLatestNotice(@Param("userId") Long userId, @Param("topic") String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(@Param("userId") Long userId, @Param("topic") String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(@Param("userId") Long userId, @Param("topic") String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(@Param("userId") Long userId, @Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);

    // 查询所有客服会话（conversation_id以service_开头）
    List<Message> selectAllServiceConversations();

    // 根据店铺ID查询客服会话
    List<Message> selectServiceConversationsByStoreId(@Param("storeId") Long storeId);

    // 查询所有门店客服会话（conversation_id以outlet_开头）
    List<Message> selectAllOutletConversations();

    // 根据服务门店ID查询门店客服会话
    List<Message> selectOutletConversationsByServiceStoreId(@Param("serviceStoreId") Long serviceStoreId);

    // 查询所有平台客服会话（platform_开头，宠物之家）
    List<Message> selectAllPlatformConversations();

    /** 用户隐藏的会话 ID 列表（用于列表过滤） */
    List<String> selectHiddenConversationIds(@Param("userId") Long userId);

    /** 记录用户隐藏某会话（重复插入忽略） */
    int insertHiddenConversation(@Param("userId") Long userId, @Param("conversationId") String conversationId);
}
