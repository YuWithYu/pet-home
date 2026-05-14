package com.pethome.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TimeSlotSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public TimeSlotSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureStoreScopedTimeSlots();
    }

    private void ensureStoreScopedTimeSlots() {
        try {
            if (!hasStoreIdColumn()) {
                log.info("time_slots 缺少 store_id 列，开始自动补齐");
                jdbcTemplate.execute(
                        "ALTER TABLE time_slots " +
                                "ADD COLUMN store_id BIGINT NULL DEFAULT NULL COMMENT '所属门店ID' AFTER service_type"
                );
            }

            if (!hasStoreIdIndex()) {
                jdbcTemplate.execute("ALTER TABLE time_slots ADD INDEX idx_store_id (store_id)");
            }

            if (hasLegacyUniqueKey()) {
                log.info("time_slots 存在旧唯一键 uk_service_time(service_type, time_slot)，开始移除");
                jdbcTemplate.execute("ALTER TABLE time_slots DROP INDEX uk_service_time");
            }

            if (!hasStoreScopedUniqueKey()) {
                log.info("time_slots 缺少按门店隔离的唯一键，开始补齐");
                jdbcTemplate.execute(
                        "ALTER TABLE time_slots " +
                                "ADD UNIQUE KEY uk_time_slots_store_service_slot (store_id, service_type, time_slot)"
                );
            }
        } catch (Exception e) {
            log.error("修复 time_slots 表结构失败，请手动执行对应 SQL", e);
        }
    }

    private boolean hasStoreIdColumn() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SHOW COLUMNS FROM time_slots LIKE 'store_id'");
        return !rows.isEmpty();
    }

    private boolean hasStoreIdIndex() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SHOW INDEX FROM time_slots WHERE Key_name = 'idx_store_id'"
        );
        return !rows.isEmpty();
    }

    private boolean hasLegacyUniqueKey() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SHOW INDEX FROM time_slots WHERE Key_name = 'uk_service_time'"
        );
        if (rows.size() != 2) {
            return false;
        }
        List<String> columns = rows.stream()
                .sorted((a, b) -> Integer.compare(
                        ((Number) a.get("Seq_in_index")).intValue(),
                        ((Number) b.get("Seq_in_index")).intValue()))
                .map(row -> String.valueOf(row.get("Column_name")))
                .collect(Collectors.toList());
        return columns.size() == 2
                && "service_type".equals(columns.get(0))
                && "time_slot".equals(columns.get(1));
    }

    private boolean hasStoreScopedUniqueKey() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SHOW INDEX FROM time_slots");
        Map<String, List<Map<String, Object>>> grouped = rows.stream()
                .filter(row -> Integer.valueOf(0).equals(((Number) row.get("Non_unique")).intValue()))
                .collect(Collectors.groupingBy(row -> String.valueOf(row.get("Key_name"))));

        for (List<Map<String, Object>> indexRows : grouped.values()) {
            if (indexRows.size() != 3) {
                continue;
            }
            List<String> columns = indexRows.stream()
                    .sorted((a, b) -> Integer.compare(
                            ((Number) a.get("Seq_in_index")).intValue(),
                            ((Number) b.get("Seq_in_index")).intValue()))
                    .map(row -> String.valueOf(row.get("Column_name")))
                    .collect(Collectors.toList());
            if (columns.size() == 3
                    && "store_id".equals(columns.get(0))
                    && "service_type".equals(columns.get(1))
                    && "time_slot".equals(columns.get(2))) {
                return true;
            }
        }
        return false;
    }
}
