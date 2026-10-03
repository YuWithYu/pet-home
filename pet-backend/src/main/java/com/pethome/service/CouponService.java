package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Coupon;

import java.util.List;
import java.util.Map;

/**
 * 优惠券模板 Service（管理端为主）
 */
public interface CouponService {

    /** 创建优惠券（默认 draft 状态） */
    Coupon create(Coupon coupon);

    /** 更新优惠券（上架后不允许修改金额规则，防止已领用户的券价值被篡改） */
    Coupon update(Coupon coupon);

    /** 上架/下架 */
    void changeStatus(Long id, String status);

    /** 分页查询（status 可选过滤） */
    IPage<Coupon> page(Page<Coupon> page, String status, String keyword);

    /** 详情 */
    Coupon getById(Long id);

    /** 查询上架中且在领取窗口内的券（领券中心，附带当前用户已领数量） */
    List<Map<String, Object>> listActiveForCenter(Long userId);

    /** 发放统计：领用量/核销量/剩余量 */
    Map<String, Object> stats(Long couponId);

    /** 按ID批量查询模板 */
    List<Coupon> listByIds(List<Long> ids);

    /** 物理删除（仅允许草稿态或零领取记录的券，防止持券用户凭空失效） */
    void deleteIfUntouched(Long id);
}
