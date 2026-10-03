package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物就医记录：每次在医院做的项目（具体疫苗、驱虫、体检、诊疗）一条记录
 */
@Data
@TableName("pet_medical_record")
@Schema(name = "PetMedicalRecord", description = "宠物就医记录")
public class PetMedicalRecord {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "宠物ID")
    @TableField("pet_id")
    private Integer petId;

    @Schema(description = "主人用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "关联的医院预约ID")
    @TableField("hospital_appointment_id")
    private Long hospitalAppointmentId;

    @Schema(description = "类型：vaccination-疫苗, deworming-驱虫, checkup-体检, treatment-诊疗, other-其他")
    @TableField("record_type")
    private String recordType;

    @Schema(description = "具体项目名称：狂犬疫苗、猫三联、体内驱虫、基础体检等")
    @TableField("item_name")
    private String itemName;

    @Schema(description = "就诊/接种日期")
    @TableField("record_date")
    private LocalDate recordDate;

    @Schema(description = "下次接种/驱虫日期")
    @TableField("next_due_date")
    private LocalDate nextDueDate;

    @Schema(description = "医生备注")
    private String remark;

    @Schema(description = "录入人（工作人员ID）")
    @TableField("operator_id")
    private Long operatorId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
