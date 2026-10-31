package com.pethome.service.community;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Complaint;

public interface ComplaintService {
    /** 用户提交投诉（可不登录），images 为图片URL列表的JSON字符串 */
    Complaint submit(Long userId, String type, String content, String contactInfo, String images);
    /** 图片列表统一序列化为 JSON 字符串；序列化失败返回 null（不作为提交失败） */
    String serializeImages(Object images);
    /** 管理员分页查询 */
    IPage<Complaint> page(Page<Complaint> page, String status, String type);
    Complaint getById(Long id);
    boolean updateStatus(Long id, String status);
}
