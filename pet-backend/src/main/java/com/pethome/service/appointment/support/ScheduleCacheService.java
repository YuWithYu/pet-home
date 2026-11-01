package com.pethome.service.appointment.support;

import com.pethome.service.common.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 排班相关 Redis 缓存清理。
 */
@Component
public class ScheduleCacheService {

    @Autowired
    private RedisCacheService redisCacheService;

    /**
     * 清除相关缓存（按门店，保证可用人数及时更新）
     */
    public void clearRelatedCacheByStore(LocalDate date, String serviceType, Long storeId) {
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        if (serviceType != null) {
            redisCacheService.deleteCache(String.format("schedule:available:%s:%s:all", dateStr, serviceType));
            if (storeId != null) {
                redisCacheService.deleteCache(String.format("schedule:available:%s:%s:%s", dateStr, serviceType, storeId));
            }
        }
    }

    /**
     * 清除相关缓存（按成员，用于排班更新等）
     */
    public void clearRelatedCache(LocalDate date, String serviceType, Long memberId) {
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        if (serviceType != null) {
            redisCacheService.deleteCache(String.format("schedule:available:%s:%s:all", dateStr, serviceType));
        }
        if (memberId != null) {
            String workloadKey = String.format("member:workload:%d:%s", memberId, dateStr);
            redisCacheService.deleteCache(workloadKey);
        }
    }

    public void invalidateAvailableTimeSlotsCache(LocalDate date, String serviceType, Long storeId) {
        if (date == null) {
            return;
        }
        String st = serviceType != null && !serviceType.isEmpty() ? serviceType : "door-cleaning";
        clearRelatedCacheByStore(date, st, storeId);
    }
}
