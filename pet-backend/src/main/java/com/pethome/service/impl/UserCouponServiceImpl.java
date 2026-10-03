package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.common.BusinessException;
import com.pethome.entity.Coupon;
import com.pethome.entity.Product;
import com.pethome.entity.UserCoupon;
import com.pethome.mapper.CouponMapper;
import com.pethome.mapper.ProductMapper;
import com.pethome.mapper.UserCouponMapper;
import com.pethome.service.UserCouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户持券 Service 实现
 *
 * <p>并发安全设计：
 * <ul>
 *   <li>防超发：UPDATE ... WHERE received_count &lt; total_count 乐观扣减，DB 行锁天然串行化并发领取</li>
 *   <li>防重复核销：UPDATE ... WHERE status='unused' 条件更新，影响行数=0 即核销失败</li>
 *   <li>防越权：所有操作以 userId 为前置条件，不信任任何前端传入的归属信息</li>
 * </ul>
 */
@Service
public class UserCouponServiceImpl implements UserCouponService {

    private static final Logger log = LoggerFactory.getLogger(UserCouponServiceImpl.class);

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon receive(Long userId, Long couponId) {
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!"active".equals(coupon.getStatus())) {
            throw new BusinessException("该券未上架或已下架");
        }
        if (coupon.getReceiveStartTime() != null && now.isBefore(coupon.getReceiveStartTime())) {
            throw new BusinessException("该券尚未开始发放");
        }
        if (coupon.getReceiveEndTime() != null && now.isAfter(coupon.getReceiveEndTime())) {
            throw new BusinessException("该券领取已结束");
        }

        // 1. 乐观扣减额度（DB 行锁串行化并发，防止超发）：
        //    UPDATE 语句的行锁持续到事务提交，同一券的并发领取在此天然串行，
        //    因此步骤 2 的 count 校验不会出现并发漏判。
        int affected = couponMapper.incrementReceivedCount(couponId);
        if (affected == 0) {
            throw new BusinessException("该券已被领完");
        }

        // 2. 每人限领校验（扣减行锁保证并发下 count 准确）
        QueryWrapper<UserCoupon> countQ = new QueryWrapper<>();
        countQ.eq("user_id", userId).eq("coupon_id", couponId);
        Long mine = userCouponMapper.selectCount(countQ);
        int perLimit = coupon.getPerUserLimit() == null ? 1 : coupon.getPerUserLimit();
        if (mine >= perLimit) {
            // 抛出异常由 @Transactional 回滚扣减（无需手工 decrement）
            throw new BusinessException("已达每人限领数量(" + perLimit + "张)");
        }

        // 3. 落持券记录
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus("unused");
        uc.setReceiveTime(now);
        int validDays = coupon.getValidDays() == null ? 7 : coupon.getValidDays();
        uc.setValidEndTime(now.plusDays(validDays));
        uc.setCreateTime(now);
        uc.setUpdateTime(now);
        userCouponMapper.insert(uc);
        return uc;
    }

    @Override
    public List<Map<String, Object>> myCoupons(Long userId, String status) {
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        // 懒过期：先把已过期的 unused 券刷新为 expired（轻量 UPDATE，幂等）
        expireUserOverdue(userId);

        QueryWrapper<UserCoupon> q = new QueryWrapper<>();
        q.eq("user_id", userId);
        if (status != null && !status.isBlank()) {
            q.eq("status", status);
        }
        q.orderByAsc("valid_end_time");
        List<UserCoupon> coupons = userCouponMapper.selectList(q);
        if (coupons.isEmpty()) {
            return List.of();
        }

        // 批量取模板（单次 IN 查询，避免 N+1）
        Set<Long> templateIds = coupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());
        Map<Long, Coupon> templateMap = couponMapper.selectBatchIds(templateIds).stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c));

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> result = new ArrayList<>(coupons.size());
        for (UserCoupon uc : coupons) {
            Coupon template = templateMap.get(uc.getCouponId());
            if (template == null) {
                continue; // 模板被物理删除的脏数据防御
            }
            Map<String, Object> m = toCardView(uc, template);
            m.put("expired", "unused".equals(uc.getStatus()) && now.isAfter(uc.getValidEndTime()));
            result.add(m);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listUsable(Long userId, List<Long> productIds, BigDecimal totalAmount) {
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<UserCoupon> q = new QueryWrapper<>();
        q.eq("user_id", userId).eq("status", "unused").gt("valid_end_time", now);
        List<UserCoupon> mine = userCouponMapper.selectList(q);
        if (mine.isEmpty()) {
            return List.of();
        }

        Set<Long> templateIds = mine.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());
        Map<Long, Coupon> templateMap = couponMapper.selectBatchIds(templateIds).stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c));

        // 商品信息一次性查出（用于范围匹配与门槛计算）
        Map<Long, Product> productMap = loadProducts(productIds);

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : mine) {
            Coupon template = templateMap.get(uc.getCouponId());
            if (template == null) {
                continue;
            }
            Map<String, Object> m = toCardView(uc, template);
            try {
                BigDecimal discount = calcDiscount(template, productMap, totalAmount);
                m.put("usable", true);
                m.put("discountAmount", discount);
            } catch (BusinessException e) {
                // 不可用：给出原因供前端置灰展示
                m.put("usable", false);
                m.put("reason", e.getMessage());
                m.put("discountAmount", BigDecimal.ZERO);
            }
            result.add(m);
        }
        // 可用的排前面，按优惠力度降序
        result.sort((a, b) -> {
            int ua = Boolean.TRUE.equals(a.get("usable")) ? 0 : 1;
            int ub = Boolean.TRUE.equals(b.get("usable")) ? 0 : 1;
            if (ua != ub) return ua - ub;
            return ((BigDecimal) b.get("discountAmount")).compareTo((BigDecimal) a.get("discountAmount"));
        });
        return result;
    }

    @Override
    public BigDecimal validateAndCalcDiscount(Long userCouponId, Long userId,
                                              List<Long> productIds, BigDecimal totalAmount) {
        if (userCouponId == null) {
            return BigDecimal.ZERO;
        }
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null || !userId.equals(uc.getUserId())) {
            throw new BusinessException("优惠券不存在");
        }
        if (!"unused".equals(uc.getStatus())) {
            throw new BusinessException("优惠券已使用或已过期");
        }
        if (uc.getValidEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }
        Coupon template = couponMapper.selectById(uc.getCouponId());
        if (template == null || !"active".equals(template.getStatus())) {
            throw new BusinessException("优惠券已失效");
        }
        Map<Long, Product> productMap = loadProducts(productIds);
        BigDecimal discount = calcDiscount(template, productMap, totalAmount);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("优惠券不满足使用条件");
        }
        return discount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markUsed(Long userCouponId, Long userId, Long orderId) {
        if (userCouponId == null || orderId == null) {
            throw new BusinessException("核销参数缺失");
        }
        int affected = userCouponMapper.markUsed(userCouponId, userId, orderId, LocalDateTime.now());
        if (affected == 0) {
            throw new BusinessException("优惠券核销失败（已使用/过期/不存在）");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnByOrder(Long orderId, Long userId) {
        if (orderId == null) {
            return;
        }
        int affected = userCouponMapper.returnByOrder(orderId, userId);
        if (affected > 0) {
            log.info("订单取消，优惠券已返还: orderId={}, userId={}, count={}", orderId, userId, affected);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int expireOverdue() {
        // 单条批量 UPDATE 替代「selectList + 逐条 updateById」的 N+1 写法
        UpdateWrapper<UserCoupon> uw = new UpdateWrapper<>();
        uw.eq("status", "unused")
                .lt("valid_end_time", LocalDateTime.now())
                .set("status", "expired")
                .set("update_time", LocalDateTime.now());
        int affected = userCouponMapper.update(null, uw);
        if (affected > 0) {
            log.info("定时过期处理: {} 张券已置为 expired", affected);
        }
        return affected;
    }

    // ==================== 私有方法 ====================

    /** 用户维度的懒过期（刷新卡包时触发；单条 UPDATE，幂等） */
    private void expireUserOverdue(Long userId) {
        UpdateWrapper<UserCoupon> uw = new UpdateWrapper<>();
        uw.eq("user_id", userId)
                .eq("status", "unused")
                .lt("valid_end_time", LocalDateTime.now())
                .set("status", "expired")
                .set("update_time", LocalDateTime.now());
        userCouponMapper.update(null, uw);
    }

    /**
     * 计算优惠金额：范围匹配 → 门槛校验 → 按券型计算。
     *
     * @throws BusinessException 券不满足使用条件
     */
    private BigDecimal calcDiscount(Coupon template, Map<Long, Product> productMap, BigDecimal totalAmount) {
        // 1. 适用范围匹配
        String scope = template.getScope() == null ? "all" : template.getScope();
        if ("product".equals(scope)) {
            // scope_value 为商品ID字符串（兼容历史数值型数据），解析后匹配
            Long productId = parseProductId(template.getScopeValue());
            if (productId == null || !productMap.containsKey(productId)) {
                throw new BusinessException("订单中不包含指定商品");
            }
        } else if ("category".equals(scope)) {
            Set<String> categories = productMap.values().stream()
                    .map(Product::getCategory)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.toSet());
            // scope_value 存分类名称（与 Product.category 对齐）
            String expect = template.getScopeValue();
            if (expect == null || expect.isBlank() || !categories.contains(expect)) {
                throw new BusinessException("订单中不包含指定分类商品");
            }
        }
        // all：全场可用

        // 2. 门槛校验
        BigDecimal threshold = template.getThresholdAmount() == null ? BigDecimal.ZERO : template.getThresholdAmount();
        if (totalAmount.compareTo(threshold) < 0) {
            throw new BusinessException(threshold.compareTo(BigDecimal.ZERO) > 0
                    ? "订单金额未满足满" + threshold.stripTrailingZeros().toPlainString() + "元门槛"
                    : "订单金额不满足使用条件");
        }

        // 3. 按类型计算
        BigDecimal discount;
        if ("discount".equals(template.getType())) {
            BigDecimal rate = template.getDiscountRate();
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) >= 0) {
                throw new BusinessException("券折扣率配置异常");
            }
            discount = totalAmount.multiply(BigDecimal.ONE.subtract(rate))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal cap = template.getMaxDiscountAmount();
            if (cap != null && discount.compareTo(cap) > 0) {
                discount = cap;
            }
        } else {
            discount = template.getDiscountAmount();
            if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("券面额配置异常");
            }
        }
        // 优惠不可超过订单金额
        return discount.min(totalAmount);
    }

    /** 解析商品范围值为商品ID（非法/空返回 null） */
    private Long parseProductId(String scopeValue) {
        if (scopeValue == null || scopeValue.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(scopeValue.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<Long, Product> loadProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new HashMap<>();
        }
        return productMapper.selectBatchIds(new ArrayList<>(new HashSet<>(productIds))).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    private Map<String, Object> toCardView(UserCoupon uc, Coupon template) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", uc.getId());
        m.put("couponId", template.getId());
        m.put("name", template.getName());
        m.put("type", template.getType());
        m.put("status", uc.getStatus());
        m.put("thresholdAmount", template.getThresholdAmount());
        m.put("discountAmount", template.getDiscountAmount());
        m.put("discountRate", template.getDiscountRate());
        m.put("maxDiscountAmount", template.getMaxDiscountAmount());
        m.put("scope", template.getScope());
        m.put("scopeValue", template.getScopeValue());
        m.put("receiveTime", uc.getReceiveTime());
        m.put("validEndTime", uc.getValidEndTime());
        m.put("orderId", uc.getOrderId());
        m.put("useTime", uc.getUseTime());
        m.put("description", template.getDescription());
        return m;
    }
}
