package com.pethome.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * <p>统一填充 create_time、update_time 审计字段，避免各 Service 手动 setCreateTime(new Date())
 * 出现的漏填、误填与时间不一致问题。
 *
 * <p>使用方式：实体字段上加
 * <pre>
 *   @TableField(value = "create_time", fill = FieldFill.INSERT)
 *   @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
 * </pre>
 * 未声明 fill 的字段不会被本处理器触碰，保持向后兼容（历史代码手动 setTime 仍有效）。
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private static final Logger log = LoggerFactory.getLogger(MybatisPlusMetaObjectHandler.class);

    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_TIME = "updateTime";

    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        try {
            // 仅当字段声明了 FieldFill 且当前值为 null 时填充；避免覆盖业务层显式设置的时间
            if (metaObject.hasSetter(CREATE_TIME) && getFieldValByName(CREATE_TIME, metaObject) == null) {
                this.strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
            }
            if (metaObject.hasSetter(UPDATE_TIME) && getFieldValByName(UPDATE_TIME, metaObject) == null) {
                this.strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);
            }
        } catch (Exception e) {
            // 降级：填充失败不应阻断主流程（部分实体可能没有这些字段）
            log.debug("MyBatis-Plus insertFill 跳过: {}", e.getMessage());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject == null) {
            return;
        }
        try {
            if (metaObject.hasSetter(UPDATE_TIME)) {
                // updateFill 默认覆盖策略：每次更新都刷新 update_time
                this.strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
            }
        } catch (Exception e) {
            log.debug("MyBatis-Plus updateFill 跳过: {}", e.getMessage());
        }
    }
}
