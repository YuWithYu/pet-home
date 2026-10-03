package com.pethome.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 优惠券系统 Schema 初始化器
 *
 * <p>幂等设计：每次启动检查表/列/索引是否存在，缺失才执行 DDL，
 * 与 TimeSlotSchemaInitializer 模式一致，避免手工执行 SQL 的运维成本。
 *
 * <p>包含：
 * <ul>
 *   <li>coupon —— 优惠券模板（管理端维护，含库存/领取窗口/限领数/适用范围）</li>
 *   <li>user_coupon —— 用户持券实例（领取/核销/返还/过期的生命周期记录）</li>
 *   <li>pet_order 增列 coupon_id / coupon_discount —— 财务可追溯（订单用了哪张券、优惠了多少）</li>
 * </ul>
 */
@Component
public class CouponSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CouponSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public CouponSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            createCouponTable();
            createUserCouponTable();
            addOrderCouponColumns();
            ensureExpireScanIndex();
        } catch (Exception e) {
            log.error("优惠券表结构初始化失败，请手动执行对应 SQL", e);
        }
    }

    /**
     * 过期扫描索引：定时任务与卡包懒过期均按 (status, valid_end_time) 全表扫描，
     * 无索引时 user_coupon 数据量增长后将退化为全表扫描。对已存在的旧表幂等补建。
     */
    private void ensureExpireScanIndex() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SHOW INDEX FROM user_coupon WHERE Key_name = 'idx_uc_expire'");
        if (rows.isEmpty()) {
            log.info("user_coupon 缺少过期扫描索引 idx_uc_expire，开始补齐");
            jdbcTemplate.execute(
                    "ALTER TABLE user_coupon ADD INDEX idx_uc_expire (status, valid_end_time)");
        }
    }

    private void createCouponTable() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SHOW TABLES LIKE 'coupon'");
        if (!rows.isEmpty()) {
            return;
        }
        log.info("coupon 表不存在，开始创建");
        jdbcTemplate.execute(
                "CREATE TABLE coupon (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(100) NOT NULL COMMENT '券名称'," +
                        "type VARCHAR(20) NOT NULL DEFAULT 'full_reduction' COMMENT '类型：full_reduction满减/discount折扣'," +
                        "threshold_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额（满X元可用，0=无门槛）'," +
                        "discount_amount DECIMAL(10,2) NULL COMMENT '满减金额（满减券必填）'," +
                        "discount_rate DECIMAL(5,2) NULL COMMENT '折扣率（折扣券，0.85=85折）'," +
                        "max_discount_amount DECIMAL(10,2) NULL COMMENT '折扣券最高优惠金额（空=不封顶）'," +
                        "total_count INT NOT NULL DEFAULT 0 COMMENT '发行总量（0=不限量）'," +
                        "received_count INT NOT NULL DEFAULT 0 COMMENT '已领取数量'," +
                        "per_user_limit INT NOT NULL DEFAULT 1 COMMENT '每人限领'," +
                        "scope VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT '适用范围：all全部/category分类/product商品'," +
                        "scope_value BIGINT NULL COMMENT '范围值（分类ID或商品ID，scope=all时为空）'," +
                        "receive_start_time DATETIME NULL COMMENT '可领取开始时间（空=立即）'," +
                        "receive_end_time DATETIME NULL COMMENT '可领取结束时间'," +
                        "valid_days INT NOT NULL DEFAULT 7 COMMENT '领取后有效天数'," +
                        "status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft草稿/active已上架/offline已下架'," +
                        "description VARCHAR(200) NULL COMMENT '使用说明'," +
                        "create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "INDEX idx_coupon_status (status)," +
                        "INDEX idx_coupon_scope (scope, scope_value)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板'"
        );
    }

    private void createUserCouponTable() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SHOW TABLES LIKE 'user_coupon'");
        if (!rows.isEmpty()) {
            return;
        }
        log.info("user_coupon 表不存在，开始创建");
        jdbcTemplate.execute(
                "CREATE TABLE user_coupon (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id BIGINT NOT NULL COMMENT '持券用户'," +
                        "coupon_id BIGINT NOT NULL COMMENT '关联优惠券模板'," +
                        "status VARCHAR(20) NOT NULL DEFAULT 'unused' COMMENT '状态：unused未使用/used已使用/expired已过期'," +
                        "receive_time DATETIME NOT NULL COMMENT '领取时间'," +
                        "valid_end_time DATETIME NOT NULL COMMENT '失效时间（领取时间+valid_days）'," +
                        "order_id BIGINT NULL COMMENT '核销的订单ID'," +
                        "use_time DATETIME NULL COMMENT '核销时间'," +
                        "create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "INDEX idx_uc_user_status (user_id, status)," +
                        "INDEX idx_uc_coupon (coupon_id)," +
                        "INDEX idx_uc_order (order_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户持券'"
        );
    }

    /** pet_order 增加优惠券两列（可空，对存量订单与现有代码零影响） */
    private void addOrderCouponColumns() {
        if (!columnExists("pet_order", "coupon_id")) {
            log.info("pet_order 缺少 coupon_id 列，开始补齐");
            jdbcTemplate.execute(
                    "ALTER TABLE pet_order ADD COLUMN coupon_id BIGINT NULL COMMENT '使用的用户持券ID' AFTER total_amount");
        }
        if (!columnExists("pet_order", "coupon_discount")) {
            log.info("pet_order 缺少 coupon_discount 列，开始补齐");
            jdbcTemplate.execute(
                    "ALTER TABLE pet_order ADD COLUMN coupon_discount DECIMAL(10,2) NULL COMMENT '优惠券抵扣金额' AFTER coupon_id");
        }
    }

    private boolean columnExists(String table, String column) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM " + table + " LIKE '" + column + "'");
        return !rows.isEmpty();
    }
}
