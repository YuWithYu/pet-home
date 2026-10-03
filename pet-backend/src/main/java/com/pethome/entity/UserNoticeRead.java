package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户公告已读记录
 */
@Data
@TableName("user_notice_read")
public class UserNoticeRead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long noticeId;
    private LocalDateTime readAt;
}
