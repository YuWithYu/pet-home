package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物体重记录：每次称重一条，用于绘制体重曲线与趋势分析
 */
@Data
@TableName("pet_weight_record")
@Schema(name = "PetWeightRecord", description = "宠物体重记录")
public class PetWeightRecord {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "宠物ID")
    @TableField("pet_id")
    private Integer petId;

    @Schema(description = "主人用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "称重体重（kg）")
    @TableField("weight")
    private BigDecimal weight;

    @Schema(description = "称重日期")
    @TableField("record_date")
    private LocalDate recordDate;

    @Schema(description = "备注")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
