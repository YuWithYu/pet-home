package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Post;
import com.pethome.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/api/search")
@Api(tags = "搜索管理")
public class SearchController {
    
    @Autowired
    private SearchService searchService;
    
    /**
     * 首页搜索 - 商品和服务
     */
    @GetMapping("/home")
    @ApiOperation("首页搜索")
    public Result<SearchService.HomeSearchResult> searchHome(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("搜索关键词不能为空");
            }
            
            SearchService.HomeSearchResult result = searchService.searchHome(keyword);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 社区搜索 - 帖子
     */
    @GetMapping("/community")
    @ApiOperation("社区搜索")
    public Result<List<Post>> searchCommunity(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("搜索关键词不能为空");
            }
            
            List<Post> result = searchService.searchCommunity(keyword);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热搜关键词
     */
    @GetMapping("/hot")
    @ApiOperation("热搜关键词")
    public Result<List<String>> getHotKeywords() {
        try {
            List<String> hotKeywords = searchService.getHotKeywords();
            return Result.success(hotKeywords);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取热搜失败: " + e.getMessage());
        }
    }
}

