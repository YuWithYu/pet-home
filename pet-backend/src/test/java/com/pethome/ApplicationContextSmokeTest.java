package com.pethome;

import com.pethome.service.appointment.ServiceAppointmentRatingService;
import com.pethome.service.community.IMessageService;
import com.pethome.service.order.PointsExchangeService;
import com.pethome.service.order.RefundRequestService;
import com.pethome.service.order.RefundSettlementService;
import com.pethome.service.order.StoreService;
import com.pethome.service.order.StoreWarehouseService;
import com.pethome.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 启动冒烟测试。
 *
 * 目的：把编译期发现不了的装配问题前移到测试阶段——
 * 缺少 Bean、构造注入循环依赖、Mapper 扫描缺失等，都会在上下文加载时直接失败。
 *
 * 依赖本地基础设施（MySQL 3306 / Redis 6379 / RabbitMQ 5672），
 * 未启动时先执行 scripts/start-infra.ps1。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class ApplicationContextSmokeTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    /**
     * 重构后新增/重构的应用服务必须都能被容器装配出来，
     * 防止"编译通过但运行期找不到 Bean"。
     */
    @Test
    void applicationServicesAreWired() {
        assertNotNull(context.getBean(PointsExchangeService.class), "积分兑换服务未装配");
        assertNotNull(context.getBean(RefundSettlementService.class), "退款结算服务未装配");
        assertNotNull(context.getBean(RefundRequestService.class), "退款单查询服务未装配");
        assertNotNull(context.getBean(StoreWarehouseService.class), "发货仓服务未装配");
        assertNotNull(context.getBean(StoreService.class), "店铺服务未装配");
        assertNotNull(context.getBean(ServiceAppointmentRatingService.class), "服务评价服务未装配");
        assertNotNull(context.getBean(IMessageService.class), "私信服务未装配");
        assertNotNull(context.getBean(UserService.class), "用户服务未装配");
    }

}
