package com.pethome.service;

import com.pethome.entity.PetMedicalRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 宠物就医记录：按次记录疫苗/驱虫/体检/诊疗，用户与医生可查
 */
public interface PetMedicalRecordService {

    /**
     * 按宠物ID查询就医记录（时间倒序）
     */
    List<PetMedicalRecord> listByPetId(Integer petId);

    /**
     * 按预约ID查询该次预约产生的就医记录
     */
    List<PetMedicalRecord> listByAppointmentId(Long hospitalAppointmentId);

    /**
     * 新增就医记录（工作人员或系统）
     */
    PetMedicalRecord add(PetMedicalRecord record);

    /**
     * 更新就医记录
     */
    PetMedicalRecord update(PetMedicalRecord record);

    /**
     * 按ID删除就医记录
     */
    void deleteById(Long id);

    /**
     * 按ID查询
     */
    PetMedicalRecord getById(Long id);

    /**
     * 查询指定宠物未来 days 天内到期的疫苗/驱虫记录（next_due_date 在今天~today+days 之间，含已过期）
     */
    List<PetMedicalRecord> listUpcoming(Integer petId, int days);

    /**
     * 查询某用户全部宠物未来 days 天内到期的记录（健康提醒用）
     */
    List<PetMedicalRecord> listUpcomingByUser(Long userId, int days);

    /**
     * 解析记录类型中文文案
     */
    default String typeText(String recordType) {
        if (recordType == null) return "其他";
        switch (recordType) {
            case "vaccination": return "疫苗";
            case "deworming": return "驱虫";
            case "checkup": return "体检";
            case "treatment": return "诊疗";
            default: return "其他";
        }
    }

    /**
     * 计算距离到期日的天数（负数表示已过期）
     */
    default long daysUntil(LocalDate dueDate) {
        if (dueDate == null) return Long.MAX_VALUE;
        return LocalDate.now().until(dueDate).getDays();
    }
}
