package com.pethome;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;

/**
 * 宠物之家后端应用启动类
 *
 * @author PetHome
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.pethome.mapper")
@EnableTransactionManagement
@EnableAsync
@org.springframework.scheduling.annotation.EnableScheduling
public class PetHomeApplication implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(PetHomeApplication.class);

    /** 应用启动开始时间，用于计算真实启动耗时 */
    private static final long START_TIME = System.currentTimeMillis();

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static void main(String[] args) {
        // 服务器无图形界面时，图片压缩（ImageIO/BufferedImage）必须启用 headless
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(PetHomeApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        long startupMillis = System.currentTimeMillis() - START_TIME;

        String sslEnabled = event.getApplicationContext().getEnvironment().getProperty("server.ssl.enabled", "false");
        String serverPort = event.getApplicationContext().getEnvironment().getProperty("server.port", "8080");
        String httpPort = event.getApplicationContext().getEnvironment().getProperty("server.http.port", "8080");

        if ("true".equals(sslEnabled)) {
            log.info("🚀 宠物之家后端服务启动成功 | HTTP: http://localhost:{} | HTTPS: https://localhost:{} | 耗时: {}ms",
                    httpPort, serverPort, startupMillis);
        } else {
            log.info("🚀 宠物之家后端服务启动成功 | 地址: http://localhost:{} | 耗时: {}ms",
                    serverPort, startupMillis);
        }

        // 启用 Redis key 过期通知，用于时间驱动的预约状态变更
        // 失败不阻断启动：定时任务会兜底处理
        try {
            stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().setConfig("notify-keyspace-events", "Ex");
            log.info("Redis key 过期通知已开启（时间驱动预约状态变更已就绪）");
        } catch (Exception e) {
            log.warn("Redis key 过期通知配置失败，将使用定时任务兜底: {}", e.getMessage());
        }
    }
}

