package com.pethome.service.user.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Post;
import com.pethome.service.community.PostService;
import com.pethome.service.user.UserContentAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserContentAppServiceImpl implements UserContentAppService {

    @Autowired
    private PostService postService;

    @Override
    public Map<String, Object> getUserPosts(Long userId, Integer page, Integer size) {
        Page<Post> postPage = new Page<>(page, size);
        IPage<Post> result = postService.getPostList(postPage, null, userId);

        // 转换为前端格式（只需要 id, content, image）
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Post post : result.getRecords()) {
            Map<String, Object> postInfo = new HashMap<>();
            postInfo.put("id", post.getId());
            postInfo.put("content", post.getContent() != null ? post.getContent() : "");
            // 如果images是多个，取第一个作为image
            String image = null;
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                String[] images = post.getImages().split(",");
                if (images.length > 0) {
                    image = images[0].trim();
                }
            }
            postInfo.put("image", image);
            resultList.add(postInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("list", resultList);
        response.put("hasMore", result.getCurrent() < result.getPages());
        return response;
    }
}
