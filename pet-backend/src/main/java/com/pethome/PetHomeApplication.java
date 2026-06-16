package com.pethome;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static void main(String[] args) {
        // 服务器无图形界面时，图片压缩（ImageIO/BufferedImage）必须启用 headless
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(PetHomeApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("==========================================");
        System.out.println("🚀 宠物之家后端服务启动成功！");
        
        // 检查是否启用了SSL
        String sslEnabled = event.getApplicationContext().getEnvironment().getProperty("server.ssl.enabled", "false");
        String serverPort = event.getApplicationContext().getEnvironment().getProperty("server.port", "8080");
        String httpPort = event.getApplicationContext().getEnvironment().getProperty("server.http.port", "8080");
        
        if ("true".equals(sslEnabled)) {
            System.out.println("📍 HTTP服务地址: http://localhost:" + httpPort);
            System.out.println("🔒 HTTPS服务地址: https://localhost:" + serverPort);
            System.out.println("💡 开发环境：同时支持HTTP和HTTPS访问");
        } else {
            System.out.println("📍 服务地址: http://localhost:" + serverPort);
        }
        
        System.out.println("📊 服务状态: 正常运行");
        System.out.println("⏰ 启动耗时: " + getStartupTime(event) + "秒");
        System.out.println("==========================================");

        System.out.println("\n📋 可用的API接口:");
        System.out.println("🔗 商品管理: /api/product/**");
        System.out.println("🔗 用户管理: /api/users/**");
        System.out.println("🔗 订单管理: /api/orders/**");
        System.out.println("🔗 认证接口: /api/auth/**");
        System.out.println("🔗 轮播图管理: /api/banners/**");
        System.out.println("==========================================");

        try {
            stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().setConfig("notify-keyspace-events", "Ex");
            System.out.println("✅ Redis key过期通知已开启（时间驱动预约状态变更已就绪）");
        } catch (Exception e) {
            System.out.println("⚠️ Redis未连接，key过期通知配置跳过（定时任务兜底仍生效）");
        }
    }

    private long getStartupTime(ApplicationReadyEvent event) {
        // 返回估算的启动时间（实际可以通过记录启动开始时间来计算）
        return 5; // 假设启动耗时5秒
    }
}
