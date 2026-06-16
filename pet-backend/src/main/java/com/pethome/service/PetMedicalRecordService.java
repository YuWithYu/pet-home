package com.pethome.service;

import com.pethome.entity.PetMedicalRecord;

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
     * 按ID查询
     */
    PetMedicalRecord getById(Long id);
}
