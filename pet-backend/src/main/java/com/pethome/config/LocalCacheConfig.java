package com.pethome.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/**
 * 本地一级缓存（Caffeine）。
 *
 * 用于管理后台聚合类统计接口：这类数据全表聚合、耗时高、容忍 60 秒延迟。
 * 相比旧实现（volatile 变量 + synchronized 双检锁）：
 * 1. get(key, loader) 内部按 key 加锁，天然单飞（single-flight），不会缓存穿透到数据库；
 * 2. 容量上限 + 过期淘汰，不会因 key 增长而无限占用内存；
 * 3. recordStats 提供命中率，可与 Redis 组成「本地 + 分布式」两级缓存。
 */
@Configuration
public class LocalCacheConfig {

    @Bean
    public Cache<String, Map<String, Object>> dashboardStatisticsCache() {
        return Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(Duration.ofSeconds(60))
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, Map<String, Object>> communityStatisticsCache() {
        return Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(Duration.ofSeconds(60))
                .recordStats()
                .build();
    }
}
