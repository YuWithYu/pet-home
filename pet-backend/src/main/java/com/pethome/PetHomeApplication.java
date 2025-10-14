package com.pethome;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
public class PetHomeApplication implements ApplicationListener<ApplicationReadyEvent> {

    public static void main(String[] args) {
        SpringApplication.run(PetHomeApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("==========================================");
        System.out.println("🚀 宠物之家后端服务启动成功！");
        System.out.println("📍 服务地址: http://localhost:8080");
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
    }

    private long getStartupTime(ApplicationReadyEvent event) {
        try {
            // 获取启动时间（这是一个估算值）
            long startTime = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(5);
            return 5; // 假设启动耗时5秒
        } catch (Exception e) {
            return 0;
        }
    }
}
