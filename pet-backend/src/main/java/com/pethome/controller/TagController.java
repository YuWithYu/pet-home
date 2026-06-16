package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Post;
import com.pethome.entity.Tag;
import com.pethome.service.PostService;
import com.pethome.service.TagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/tag")
@Api(tags = "标签管理")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private PostService postService;
    
    @GetMapping("/hot")
    @ApiOperation("获取热门标签")
    public Result<List<String>> getHotTags(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<String> hotTags = tagService.getHotTags(limit);
            return Result.success(hotTags);
        } catch (Exception e) {
            return Result.error("获取热门标签失败: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    @ApiOperation("获取全部标签（发现页标签栏）")
    public Result<List<String>> getAllTags() {
        try {
            return Result.success(tagService.getAllTagNames());
        } catch (Exception e) {
            return Result.error("获取标签列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/post/{postId}")
    @ApiOperation("获取帖子的标签列表")
    public Result<List<Tag>> getPostTags(@PathVariable Long postId) {
        try {
            List<Tag> tags = tagService.getTagsByPostId(postId);
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error("获取帖子标签失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/posts")
    @ApiOperation("根据标签查询帖子")
    public Result<Map<String, Object>> getPostsByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            // 根据标签名获取帖子ID列表
            List<Long> postIds = tagService.getPostIdsByTagName(tag);
            
            if (postIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("posts", new ArrayList<>());
                response.put("total", 0);
                response.put("pages", 0);
                response.put("current", page);
                response.put("size", size);
                return Result.success(response);
            }
            
            // 根据帖子ID列表查询帖子详情（简化处理，实际可能需要分页优化）
            List<Post> posts = new ArrayList<>();
            for (Long postId : postIds) {
                Post post = postService.getPostDetail(postId, null);
                if (post != null && post.getStatus() == 1) {
                    posts.add(post);
                }
            }
            
            // 简单分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, posts.size());
            List<Post> pagedPosts = start < posts.size() ? posts.subList(start, end) : new ArrayList<>();
            
            Map<String, Object> response = new HashMap<>();
            response.put("posts", pagedPosts);
            response.put("total", posts.size());
            response.put("pages", (int) Math.ceil((double) posts.size() / size));
            response.put("current", page);
            response.put("size", size);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("根据标签查询帖子失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/recommend")
    @ApiOperation("获取推荐帖子（基于热门标签）")
    public Result<List<Post>> getRecommendPosts(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            // 获取热门标签
            List<String> hotTags = tagService.getHotTags(10);
            
            if (hotTags.isEmpty()) {
                // 如果没有热门标签，返回普通热门帖子
                List<Post> hotPosts = postService.getHotPosts(limit);
                return Result.success(hotPosts);
            }
            
            // 随机选择一个热门标签
            Random random = new Random();
            String selectedTag = hotTags.get(random.nextInt(hotTags.size()));
            
            // 根据标签获取帖子
            List<Long> postIds = tagService.getPostIdsByTagName(selectedTag);
            
            if (postIds.isEmpty()) {
                List<Post> hotPosts = postService.getHotPosts(limit);
                return Result.success(hotPosts);
            }
            
            // 获取帖子详情（限制数量）
            List<Post> posts = new ArrayList<>();
            int count = 0;
            for (Long postId : postIds) {
                if (count >= limit) break;
                Post post = postService.getPostDetail(postId, null);
                if (post != null && post.getStatus() == 1) {
                    posts.add(post);
                    count++;
                }
            }
            
            // 如果数量不足，补充热门帖子
            if (posts.size() < limit) {
                List<Post> hotPosts = postService.getHotPosts(limit - posts.size());
                posts.addAll(hotPosts);
            }
            
            return Result.success(posts);
        } catch (Exception e) {
            return Result.error("获取推荐帖子失败: " + e.getMessage());
        }
    }

    @PostMapping("/admin/create")
    @ApiOperation("管理员新增标签")
    public Result<Tag> createTag(@RequestBody Map<String, String> request) {
        try {
            String name = request != null ? request.get("name") : null;
            Tag tag = tagService.createTag(name);
            if (tag == null) {
                return Result.error("标签名不能为空");
            }
            return Result.success(tag);
        } catch (Exception e) {
            return Result.error("新增标签失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/{name}")
    @ApiOperation("管理员删除标签（推荐标签不可删除）")
    public Result<Boolean> deleteTag(@PathVariable String name) {
        try {
            boolean ok = tagService.deleteTagByName(name);
            if (!ok) {
                return Result.error("删除失败：标签不存在或为受保护标签");
            }
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("删除标签失败: " + e.getMessage());
        }
    }
}

