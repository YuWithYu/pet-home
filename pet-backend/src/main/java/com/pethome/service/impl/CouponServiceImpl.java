package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.BusinessException;
import com.pethome.entity.Coupon;
import com.pethome.entity.UserCoupon;
import com.pethome.mapper.CouponMapper;
import com.pethome.mapper.UserCouponMapper;
import com.pethome.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 优惠券模板 Service 实现
 */
@Service
public class CouponServiceImpl implements CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponServiceImpl.class);

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon create(Coupon coupon) {
        validate(coupon);
        coupon.setId(null);
        coupon.setStatus("draft");
        coupon.setReceivedCount(0);
        couponMapper.insert(coupon);
        return coupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon update(Coupon coupon) {
        if (coupon == null || coupon.getId() == null) {
            throw new BusinessException("优惠券ID不能为空");
        }
        Coupon existing = couponMapper.selectById(coupon.getId());
        if (existing == null) {
            throw new BusinessException("优惠券不存在");
        }
        // 资金规则保护：已上架且有领取记录的券，禁止修改金额/门槛/类型（防已领用户权益被篡改）
        boolean hasReceived = existing.getReceivedCount() != null && existing.getReceivedCount() > 0;
        if (hasReceived) {
            boolean ruleChanged = !eq(existing.getThresholdAmount(), coupon.getThresholdAmount())
                    || !eq(existing.getDiscountAmount(), coupon.getDiscountAmount())
                    || !eq(existing.getDiscountRate(), coupon.getDiscountRate())
                    || !eq(existing.getMaxDiscountAmount(), coupon.getMaxDiscountAmount())
                    || !strEq(existing.getType(), coupon.getType());
            if (ruleChanged) {
                throw new BusinessException("该券已有用户领取，不可修改金额规则（可下架后新建）");
            }
        }
        // 修复：update 此前完全跳过校验，导致空名称/非法折扣率/满减倒挂等可经编辑写入
        validate(coupon);
        // 归属保护字段
        coupon.setReceivedCount(existing.getReceivedCount());
        couponMapper.updateById(coupon);
        return couponMapper.selectById(coupon.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, String status) {
        if (!"active".equals(status) && !"offline".equals(status) && !"draft".equals(status)) {
            throw new BusinessException("非法状态: " + status);
        }
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        coupon.setStatus(status);
        couponMapper.updateById(coupon);
    }

    @Override
    public IPage<Coupon> page(Page<Coupon> page, String status, String keyword) {
        QueryWrapper<Coupon> q = new QueryWrapper<>();
        if (status != null && !status.isBlank()) {
            q.eq("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            q.like("name", keyword);
        }
        q.orderByDesc("create_time");
        return couponMapper.selectPage(page, q);
    }

    @Override
    public Coupon getById(Long id) {
        return couponMapper.selectById(id);
    }

    @Override
    public List<Map<String, Object>> listActiveForCenter(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Coupon> q = new QueryWrapper<>();
        q.eq("status", "active")
                .and(w -> w.isNull("receive_start_time").or().le("receive_start_time", now))
                .and(w -> w.isNull("receive_end_time").or().ge("receive_end_time", now))
                .orderByDesc("create_time");
        List<Coupon> coupons = couponMapper.selectList(q);

        // 批量查询当前用户对这些券的已领数量（单次 IN 查询，避免 N+1）
        Map<Long, Long> receivedCountMap = new HashMap<>();
        if (userId != null && !coupons.isEmpty()) {
            List<Long> couponIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());
            QueryWrapper<UserCoupon> uq = new QueryWrapper<>();
            uq.eq("user_id", userId).in("coupon_id", couponIds);
            List<UserCoupon> mine = userCouponMapper.selectList(uq);
            receivedCountMap = mine.stream().collect(
                    Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
        }

        List<Map<String, Object>> result = new ArrayList<>(coupons.size());
        for (Coupon c : coupons) {
            Map<String, Object> m = toCenterView(c);
            long received = receivedCountMap.getOrDefault(c.getId(), 0L);
            m.put("received", received);
            // 每人限领与剩余量判断（不限量券 remainingOf 为 MAX_VALUE，不透出到 API）
            int perLimit = c.getPerUserLimit() == null ? 1 : c.getPerUserLimit();
            m.put("canReceive", received < perLimit && remainingOf(c) > 0);
            result.add(m);
        }
        return result;
    }

    @Override
    public Map<String, Object> stats(Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }

        // 单次 GROUP BY 统计三种状态数量（替代 3 次独立 COUNT 的数据库往返）
        QueryWrapper<UserCoupon> gq = new QueryWrapper<>();
        gq.select("status", "COUNT(*) AS cnt")
                .eq("coupon_id", couponId)
                .groupBy("status");
        Map<String, Long> statusCount = new HashMap<>();
        for (Map<String, Object> row : userCouponMapper.selectMaps(gq)) {
            Object status = row.get("status");
            Object cnt = row.get("cnt");
            if (status != null && cnt != null) {
                statusCount.put(status.toString(), Long.parseLong(cnt.toString()));
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", coupon.getTotalCount());
        stats.put("receivedCount", coupon.getReceivedCount());
        stats.put("usedCount", statusCount.getOrDefault("used", 0L));
        stats.put("unusedCount", statusCount.getOrDefault("unused", 0L));
        stats.put("expiredCount", statusCount.getOrDefault("expired", 0L));
        // 不限量时前端展示"不限"，避免 Long.MAX_VALUE 直接序列化到 API
        long remaining = remainingOf(coupon);
        stats.put("remaining", remaining == Long.MAX_VALUE ? -1 : remaining);
        return stats;
    }

    @Override
    public List<Coupon> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return couponMapper.selectBatchIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIfUntouched(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (coupon.getReceivedCount() != null && coupon.getReceivedCount() > 0) {
            throw new BusinessException("该券已有领取记录，不允许删除，请改为下架");
        }
        couponMapper.deleteById(id);
    }

    // ==================== 私有方法 ====================

    private void validate(Coupon coupon) {
        if (coupon == null) {
            throw new BusinessException("优惠券数据不能为空");
        }
        if (coupon.getName() == null || coupon.getName().isBlank()) {
            throw new BusinessException("券名称不能为空");
        }
        String type = coupon.getType() == null ? "full_reduction" : coupon.getType();
        if (!"full_reduction".equals(type) && !"discount".equals(type)) {
            throw new BusinessException("不支持的优惠券类型: " + type);
        }
        if ("full_reduction".equals(type)) {
            if (coupon.getDiscountAmount() == null || coupon.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("满减券必须配置大于0的满减金额");
            }
            // 修复：防止「满10减50」类倒挂券（配合结算端 min(totalAmount) 只是无害兜底，源头应拒绝）
            BigDecimal thresholdForCap = coupon.getThresholdAmount() == null ? BigDecimal.ZERO : coupon.getThresholdAmount();
            if (thresholdForCap.compareTo(BigDecimal.ZERO) > 0
                    && coupon.getDiscountAmount().compareTo(thresholdForCap) > 0) {
                throw new BusinessException("满减金额不能大于使用门槛");
            }
        } else {
            BigDecimal rate = coupon.getDiscountRate();
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) >= 0) {
                throw new BusinessException("折扣率必须为(0,1)之间，如 0.85 表示85折");
            }
        }
        BigDecimal threshold = coupon.getThresholdAmount() == null ? BigDecimal.ZERO : coupon.getThresholdAmount();
        if (threshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("使用门槛不能为负数");
        }
        if (coupon.getValidDays() == null || coupon.getValidDays() <= 0 || coupon.getValidDays() > 365) {
            throw new BusinessException("有效天数必须在1~365之间");
        }
        if (coupon.getPerUserLimit() == null || coupon.getPerUserLimit() <= 0 || coupon.getPerUserLimit() > 10) {
            throw new BusinessException("每人限领必须在1~10之间");
        }
        if (coupon.getTotalCount() != null && coupon.getTotalCount() < 0) {
            throw new BusinessException("发行总量不能为负数");
        }
        String scope = coupon.getScope() == null ? "all" : coupon.getScope();
        if (!"all".equals(scope) && !"category".equals(scope) && !"product".equals(scope)) {
            throw new BusinessException("不支持的适用范围: " + scope);
        }
        if (!"all".equals(scope) && coupon.getScopeValue() == null) {
            throw new BusinessException("指定分类/商品范围时必须填写范围值");
        }
        // 修复：领取窗口倒挂（start ≥ end 的券永远无法被领取，且静默无提示）
        if (coupon.getReceiveStartTime() != null && coupon.getReceiveEndTime() != null
                && !coupon.getReceiveStartTime().isBefore(coupon.getReceiveEndTime())) {
            throw new BusinessException("可领取开始时间必须早于结束时间");
        }
    }

    private long remainingOf(Coupon c) {
        if (c.getTotalCount() == null || c.getTotalCount() == 0) {
            return Long.MAX_VALUE; // 不限量
        }
        long received = c.getReceivedCount() == null ? 0 : c.getReceivedCount();
        return Math.max(0, c.getTotalCount() - received);
    }

    private Map<String, Object> toCenterView(Coupon c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("type", c.getType());
        m.put("thresholdAmount", c.getThresholdAmount());
        m.put("discountAmount", c.getDiscountAmount());
        m.put("discountRate", c.getDiscountRate());
        m.put("maxDiscountAmount", c.getMaxDiscountAmount());
        m.put("scope", c.getScope());
        m.put("scopeValue", c.getScopeValue());
        m.put("perUserLimit", c.getPerUserLimit() == null ? 1 : c.getPerUserLimit());
        m.put("validDays", c.getValidDays());
        m.put("receiveEndTime", c.getReceiveEndTime());
        m.put("description", c.getDescription());
        return m;
    }

    private boolean eq(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    private boolean strEq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
