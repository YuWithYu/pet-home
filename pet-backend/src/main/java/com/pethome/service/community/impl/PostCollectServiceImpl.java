package com.pethome.service.community.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Post;
import com.pethome.entity.PostCollect;
import com.pethome.mapper.PostCollectMapper;
import com.pethome.mapper.PostMapper;
import com.pethome.service.community.PostCollectService;
import com.pethome.service.community.PostService;
import com.pethome.service.admin.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostCollectServiceImpl extends ServiceImpl<PostCollectMapper, PostCollect> implements PostCollectService {

    private static final Logger log = LoggerFactory.getLogger(PostCollectServiceImpl.class);

    @Autowired
    private PostCollectMapper postCollectMapper;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    @Lazy
    private TaskService taskService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectPost(Long postId, Long userId) {
        // 参数验证
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("帖子ID和用户ID不能为空");
        }
        
        // 检查是否已收藏
        PostCollect existingCollect = postCollectMapper.checkCollectStatus(postId, userId);
        if (existingCollect != null) {
            return false; // 已经收藏过了
        }
        
        // 添加收藏记录
        PostCollect collect = new PostCollect();
        collect.setPostId(postId);
        collect.setUserId(userId);
        collect.setCreateTime(LocalDateTime.now());
        postCollectMapper.insert(collect);
        
        // 更新帖子收藏数（如果Post实体有collectsCount字段）
        // postService.updateCollectsCount(postId, 1);
        
        // 更新任务进度：收藏内容任务
        try {
            // 获取用户今日已收藏的帖子数量
            int todayCollectCount = postCollectMapper.getTodayCollectCount(userId);
            taskService.updateTaskProgress(userId, "collect_content", todayCollectCount);
            log.debug("更新收藏内容任务进度，用户ID: {}, 今日收藏数: {}", userId, todayCollectCount);
        } catch (Exception e) {
            // 任务进度更新失败不影响收藏流程
            log.error("更新收藏内容任务进度失败", e);
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uncollectPost(Long postId, Long userId) {
        // 参数验证
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("帖子ID和用户ID不能为空");
        }
        
        // 查找收藏记录
        PostCollect collect = postCollectMapper.checkCollectStatus(postId, userId);
        if (collect == null) {
            return false; // 没有收藏记录
        }
        
        // 删除收藏记录
        postCollectMapper.deleteById(collect.getId());
        
        // 更新帖子收藏数（如果Post实体有collectsCount字段）
        // postService.updateCollectsCount(postId, -1);
        
        return true;
    }
    
    @Override
    public boolean isCollected(Long postId, Long userId) {
        PostCollect collect = postCollectMapper.checkCollectStatus(postId, userId);
        return collect != null;
    }
    
    @Override
    public Page<Post> getCollectedPosts(Long userId, Page<Post> page, String type) {
        // 查询用户收藏的帖子ID列表
        QueryWrapper<PostCollect> collectWrapper = new QueryWrapper<>();
        collectWrapper.eq("user_id", userId);
        collectWrapper.orderByDesc("create_time");
        
        List<PostCollect> collects = postCollectMapper.selectList(collectWrapper);
        
        if (collects == null || collects.isEmpty()) {
            return page; // 返回空分页
        }
        
        // 提取帖子ID列表
        List<Long> postIds = collects.stream()
            .map(PostCollect::getPostId)
            .collect(Collectors.toList());
        
        // 查询帖子详情
        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        postWrapper.in("id", postIds);
        postWrapper.eq("status", 1); // 只查询正常状态的帖子
        
        // 根据type筛选（动态或文章）
        if ("动态".equals(type) || "post".equals(type)) {
            // 动态：有图片或视频的帖子
            postWrapper.and(w -> w.isNotNull("images").or().isNotNull("cover_image"));
        } else if ("文章".equals(type) || "article".equals(type)) {
            // 文章：主要是文字内容的帖子
            postWrapper.and(w -> w.isNull("images").and(w2 -> w2.isNull("cover_image")));
        }
        
        // 按收藏时间排序（需要关联查询，这里简化处理，按帖子创建时间倒序）
        postWrapper.orderByDesc("create_time");
        
        // 分页查询
        Page<Post> result = postMapper.selectPage(page, postWrapper);
        
        // 按收藏时间重新排序（如果需要的话，可以在前端处理）
        
        return result;
    }
}

