package com.pethome.controller.order;


import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.entity.Post;
import com.pethome.service.order.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/api/search")
@Tag(name = "搜索管理")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;
    
    /**
     * 首页搜索 - 商品和服务
     */
    @GetMapping("/home")
    @Operation(summary = "首页搜索")
    public Result<SearchService.HomeSearchResult> searchHome(@RequestParam String keyword) {
        return com.pethome.common.SafeCall.of(() -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("搜索关键词不能为空");
            }
            
            SearchService.HomeSearchResult result = searchService.searchHome(keyword);
            return Result.success(result);
        }, "搜索失败，请稍后重试", log);
    }
    
    /**
     * 社区搜索 - 帖子
     */
    @GetMapping("/community")
    @Operation(summary = "社区搜索")
    public Result<List<Post>> searchCommunity(@RequestParam String keyword) {
        return com.pethome.common.SafeCall.of(() -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("搜索关键词不能为空");
            }
            
            List<Post> result = searchService.searchCommunity(keyword);
            return Result.success(result);
        }, "搜索失败，请稍后重试", log);
    }
    
    /**
     * 获取热搜关键词
     */
    @GetMapping("/hot")
    @Operation(summary = "热搜关键词")
    public Result<List<String>> getHotKeywords() {
        return com.pethome.common.SafeCall.of(() -> {
            List<String> hotKeywords = searchService.getHotKeywords();
            return Result.success(hotKeywords);
        }, "获取热搜失败，请稍后重试", log);
    }
}

