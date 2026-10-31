package com.pethome.task;

import com.pethome.service.order.UserCouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 优惠券定时任务：将过期的 unused 持券批量置为 expired
 *
 * <p>与卡包懒过期（expireUserOverdue）互为双保障：
 * 定时任务负责全局批量清理，懒过期保证用户视角即时准确。
 */
@Component
public class CouponExpireTask {

    private static final Logger log = LoggerFactory.getLogger(CouponExpireTask.class);

    @Autowired
    private UserCouponService userCouponService;

    /** 每天凌晨 3 点执行（错开订单自动完成任务的 2 点高峰） */
    @Scheduled(cron = "0 0 3 * * ?")
    public void expireOverdueCoupons() {
        try {
            int expired = userCouponService.expireOverdue();
            if (expired > 0) {
                log.info("优惠券过期任务完成: {} 张券已置为 expired", expired);
            }
        } catch (Exception e) {
            log.error("优惠券过期任务执行失败", e);
        }
    }
}
