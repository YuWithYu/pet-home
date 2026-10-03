-- ============================================================
-- 迁移脚本：优惠券范围值类型修正
-- 日期：2026-09-04
-- 背景：
--   coupon.scope_value 原为 bigint，但分类范围券的使用匹配逻辑
--   （UserCouponServiceImpl.calcDiscount）按 Product.category 分类
--   【名称】比对，bigint 无法存名称 → 分类券创建后永远无法使用。
-- 修复：
--   scope_value 改为 VARCHAR(64)：
--     scope=category → 存分类名称（与 Product.category 对齐）
--     scope=product  → 存商品ID字符串（使用时解析为 Long 匹配）
--   实体 Coupon.scopeValue 已同步改为 String。
-- ============================================================

ALTER TABLE `coupon`
    MODIFY COLUMN `scope_value` VARCHAR(64) NULL DEFAULT NULL COMMENT '范围值：scope=category 存分类名称；scope=product 存商品ID字符串';
