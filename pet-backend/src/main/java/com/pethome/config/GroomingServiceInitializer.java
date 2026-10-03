package com.pethome.config;

import com.pethome.entity.GroomingService;
import com.pethome.service.GroomingServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class GroomingServiceInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GroomingServiceService groomingServiceService;

    @PostConstruct
    public void init() {
        createTableIfNecessary();
        ensureColumns();
        seedSampleData();
    }

    private void createTableIfNecessary() {
        String sql = "CREATE TABLE IF NOT EXISTS grooming_services (" +
                "id BIGINT NOT NULL AUTO_INCREMENT," +
                "name VARCHAR(100) NOT NULL," +
                "description TEXT," +
                "introduction JSON NULL," +
                "instructions JSON NULL," +
                "price DECIMAL(10,2) NOT NULL DEFAULT 0," +
                "duration INT DEFAULT 60," +
                "category VARCHAR(50)," +
                "image_url VARCHAR(255)," +
                "bg_color VARCHAR(20)," +
                "tags JSON," +
                "status VARCHAR(20) DEFAULT 'active'," +
                "is_recommended TINYINT(1) DEFAULT 0," +
                "sort_order INT DEFAULT 0," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "is_deleted TINYINT(1) DEFAULT 0," +
                "PRIMARY KEY (id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        jdbcTemplate.execute(sql);
    }

    private void ensureColumns() {
        ensureColumn("description", "description TEXT NULL");
        ensureColumn("introduction", "introduction JSON NULL");
        ensureColumn("instructions", "instructions JSON NULL");
        ensureColumn("price", "price DECIMAL(10,2) NOT NULL DEFAULT 0");
        ensureColumn("duration", "duration INT NULL");
        ensureColumn("category", "category VARCHAR(50) NULL");
        ensureColumn("image_url", "image_url VARCHAR(255) NULL");
        ensureColumn("bg_color", "bg_color VARCHAR(20) NULL");
        ensureColumn("tags", "tags JSON NULL");
        ensureColumn("status", "status VARCHAR(20) NOT NULL DEFAULT 'active'");
        ensureColumn("is_recommended", "is_recommended TINYINT(1) NOT NULL DEFAULT 0");
        ensureColumn("sort_order", "sort_order INT NOT NULL DEFAULT 0");
        ensureColumn("created_at", "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        ensureColumn("updated_at", "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        ensureColumn("is_deleted", "is_deleted TINYINT(1) NOT NULL DEFAULT 0");
    }

    private void ensureColumn(String columnName, String definition) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'grooming_services' AND column_name = ?",
                Integer.class,
                columnName
        );
        if (exists != null && exists == 0) {
            jdbcTemplate.execute("ALTER TABLE grooming_services ADD COLUMN " + definition);
        }
    }

    private void seedSampleData() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM grooming_services", Long.class);
        if (count != null && count > 0) {
            return;
        }

        List<GroomingService> samples = Arrays.asList(
                createService(
                        "基础洗护套餐",
                        "适用于常规清洁的洗护组合，包含深层清洁与基础修剪。",
                        Arrays.asList("专业宠物洗护师全程服务", "深层清洁和吹干梳理", "基础爪毛修剪与清洁耳道"),
                        Arrays.asList("服务前请提前预约时间", "如宠物近期有皮肤问题请提前告知", "需携带宠物疫苗本以备查验"),
                        new BigDecimal("168"),
                        60,
                        "basic-care",
                        "#f2fbff",
                        Arrays.asList("洗护", "基础套餐")
                ),
                createService(
                        "SPA 按摩护理",
                        "舒缓按摩搭配营养护肤，让宠物放松身心，提升毛发光泽。",
                        Arrays.asList("精油香薰舒缓按摩", "营养护毛护理", "专属静养休息区"),
                        Arrays.asList("服务前请保持毛发干燥", "如有心脏或骨骼疾病请提前说明", "建议服务后休息30分钟"),
                        new BigDecimal("258"),
                        75,
                        "spa",
                        "#fff4ec",
                        Arrays.asList("SPA", "舒缓护理")
                ),
                createService(
                        "造型美容套餐",
                        "根据宠物特征定制专属造型，包含全套修剪与造型设计。",
                        Arrays.asList("专业造型设计咨询", "全身修剪与吹毛定型", "提供造型后拍照留念"),
                        Arrays.asList("需要提供喜欢的造型示例", "建议提前洗净并吹干基础毛发", "如需染色请提前说明"),
                        new BigDecimal("328"),
                        90,
                        "styling",
                        "#f8f5ff",
                        Arrays.asList("美容", "造型")
                )
        );

        if (!CollectionUtils.isEmpty(samples)) {
            samples.forEach(groomingServiceService::createGroomingService);
        }
    }

    private GroomingService createService(String name,
                                          String description,
                                          List<String> introduction,
                                          List<String> instructions,
                                          BigDecimal price,
                                          Integer duration,
                                          String category,
                                          String bgColor,
                                          List<String> tags) {
        GroomingService service = new GroomingService();
        service.setName(name);
        service.setDescription(description);
        service.setIntroduction(introduction);
        service.setInstructions(instructions);
        service.setPrice(price);
        service.setDuration(duration);
        service.setCategory(category);
        service.setBgColor(bgColor);
        service.setTags(tags);
        service.setImageUrl("https://localhost/static/images/pet-grooming-service.png");
        service.setStatus("active");
        service.setIsRecommended(Boolean.TRUE);
        service.setSortOrder(0);
        service.setIsDeleted(Boolean.FALSE);
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        return service;
    }
}

