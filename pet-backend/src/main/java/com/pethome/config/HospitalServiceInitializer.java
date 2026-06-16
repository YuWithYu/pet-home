package com.pethome.config;

import com.pethome.entity.HospitalService;
import com.pethome.service.HospitalServiceService;
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
public class HospitalServiceInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HospitalServiceService hospitalServiceService;

    @PostConstruct
    public void init() {
        createTableIfNecessary();
        ensureColumns();
        seedSampleData();
    }

    private void createTableIfNecessary() {
        String sql = "CREATE TABLE IF NOT EXISTS hospital_services (" +
                "id BIGINT NOT NULL AUTO_INCREMENT," +
                "name VARCHAR(100) NOT NULL," +
                "description TEXT," +
                "introduction JSON NULL," +
                "instructions JSON NULL," +
                "price DECIMAL(10,2) NOT NULL DEFAULT 0," +
                "duration INT DEFAULT 30," +
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
                "product_introduction TEXT," +
                "usage_instructions TEXT," +
                "precautions TEXT," +
                "target_audience VARCHAR(100)," +
                "service_duration VARCHAR(50)," +
                "booking_requirements TEXT," +
                "service_features TEXT," +
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
        ensureColumn("product_introduction", "product_introduction TEXT NULL");
        ensureColumn("usage_instructions", "usage_instructions TEXT NULL");
        ensureColumn("precautions", "precautions TEXT NULL");
        ensureColumn("target_audience", "target_audience VARCHAR(100) NULL");
        ensureColumn("service_duration", "service_duration VARCHAR(50) NULL");
        ensureColumn("booking_requirements", "booking_requirements TEXT NULL");
        ensureColumn("service_features", "service_features TEXT NULL");
    }

    private void ensureColumn(String columnName, String definition) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'hospital_services' AND column_name = ?",
                Integer.class,
                columnName
        );
        if (exists != null && exists == 0) {
            jdbcTemplate.execute("ALTER TABLE hospital_services ADD COLUMN " + definition);
        }
    }

    private void seedSampleData() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hospital_services", Long.class);
        if (count != null && count > 0) {
            return;
        }

        List<HospitalService> samples = Arrays.asList(
                createService(
                        "基础体检套餐",
                        "常规健康评估，包含问诊、体温、心肺、皮肤等项目，适合年度体检。",
                        Arrays.asList("专业兽医问诊评估", "基础生理指标检测", "提供健康档案"),
                        Arrays.asList("体检前请禁食8小时", "如有慢性病史请告知医生", "携带疫苗本以便登记"),
                        new BigDecimal("199"),
                        45,
                        "checkup",
                        "#eaf6ff",
                        Arrays.asList("体检", "基础服务")
                ),
                createService(
                        "疫苗及驱虫套餐",
                        "根据宠物年龄及生活习惯定制疫苗接种方案，含体重评估与驱虫建议。",
                        Arrays.asList("定制免疫方案", "体重体况评估", "驱虫计划建议"),
                        Arrays.asList("接种前需确认健康状态良好", "如近期有药物使用请提前说明", "幼宠请由监护人陪同"),
                        new BigDecimal("149"),
                        30,
                        "vaccine",
                        "#fff5e6",
                        Arrays.asList("疫苗", "驱虫", "预防保健")
                ),
                createService(
                        "外科手术术前评估",
                        "针对即将进行手术的宠物，提供全套术前检查及麻醉风险评估。",
                        Arrays.asList("血常规及生化检查", "心电图与X光检查", "麻醉风险评估报告"),
                        Arrays.asList("需提前预约安排检查时间", "手术前12小时禁食禁水", "请携带既往病历和用药记录"),
                        new BigDecimal("399"),
                        90,
                        "surgery",
                        "#f3e8ff",
                        Arrays.asList("术前评估", "高级检查")
                )
        );

        if (!CollectionUtils.isEmpty(samples)) {
            samples.forEach(hospitalServiceService::createHospitalService);
        }
    }

    private HospitalService createService(String name,
                                          String description,
                                          List<String> introduction,
                                          List<String> instructions,
                                          BigDecimal price,
                                          Integer duration,
                                          String category,
                                          String bgColor,
                                          List<String> tags) {
        HospitalService service = new HospitalService();
        service.setName(name);
        service.setDescription(description);
        service.setIntroduction(introduction);
        service.setInstructions(instructions);
        service.setPrice(price);
        service.setDuration(duration);
        service.setCategory(category);
        service.setBgColor(bgColor);
        service.setImageUrl("https://localhost/static/images/pet-hospital-service.png");
        service.setTags(tags);
        service.setStatus("active");
        service.setIsRecommended(Boolean.TRUE);
        service.setSortOrder(0);
        service.setIsDeleted(false);
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        return service;
    }
}

