package com.pethome.service.impl;

import com.pethome.entity.Product;
import com.pethome.entity.Post;
import com.pethome.entity.ServiceStore;
import com.pethome.mapper.SearchMapper;
import com.pethome.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 */
@Service
public class SearchServiceImpl implements SearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
    
    private static final String HOT_KEYWORDS_KEY = "hot_keywords";
    
    @Autowired
    private SearchMapper searchMapper;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public HomeSearchResult searchHome(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new HomeSearchResult(new ArrayList<>(), new ArrayList<>());
        }
        
        keyword = keyword.trim();
        
        // 记录搜索热词
        incrementHotKeyword(keyword);
        
        // 搜索商品和服务
        List<Product> products = searchMapper.searchProducts(keyword);
        List<ServiceStore> services = searchMapper.searchServices(keyword);
        
        return new HomeSearchResult(products, services);
    }
    
    @Override
    public List<Post> searchCommunity(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        keyword = keyword.trim();
        
        // 记录搜索热词
        incrementHotKeyword(keyword);
        
        // 搜索帖子
        return searchMapper.searchPosts(keyword);
    }
    
    private static final List<String> DEFAULT_HOT_KEYWORDS = Arrays.asList(
            "猫粮", "狗粮", "宠物医疗", "美容服务", "宠物玩具", "宠物零食"
    );

    @Override
    public List<String> getHotKeywords() {
        if (isRedisAvailable()) {
            try {
                Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                        .reverseRangeWithScores(HOT_KEYWORDS_KEY, 0, 9);
                if (tuples != null && !tuples.isEmpty()) {
                    List<String> list = tuples.stream()
                            .map(tuple -> tuple.getValue() != null ? tuple.getValue().toString() : null)
                            .filter(keyword -> keyword != null)
                            .collect(Collectors.toList());
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (Exception e) {
                logger.error("获取热搜词失败: " + e.getMessage(), e);
            }
        }
        // Redis 不可用或无数据时返回默认热搜，避免前端写死
        return new ArrayList<>(DEFAULT_HOT_KEYWORDS);
    }
    
    /**
     * 增加热词分数
     */
    private void incrementHotKeyword(String keyword) {
        if (!isRedisAvailable()) {
            return;
        }
        
        try {
            redisTemplate.opsForZSet().incrementScore(HOT_KEYWORDS_KEY, keyword, 1);
        } catch (Exception e) {
            logger.error("更新热词失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查Redis是否可用
     */
    private boolean isRedisAvailable() {
        if (redisTemplate == null) {
            return false;
        }
        try {
            redisTemplate.opsForValue().get("test");
            return true;
        } catch (Exception e) {
            logger.warn("Redis不可用", e);
            return false;
        }
    }
}

