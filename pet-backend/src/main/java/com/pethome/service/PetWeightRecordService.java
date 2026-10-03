package com.pethome.service;

import com.pethome.entity.PetWeightRecord;

import java.util.List;

/**
 * 宠物体重记录：用于体重曲线与趋势分析
 */
public interface PetWeightRecordService {

    /** 按宠物查询体重记录（按称重日期正序，便于绘图） */
    List<PetWeightRecord> listByPetId(Integer petId);

    /** 新增体重记录（同一天重复记录则覆盖更新） */
    PetWeightRecord add(PetWeightRecord record);

    /** 更新体重记录（仅允许修改 weight/recordDate/remark；归属与宠物不可变） */
    PetWeightRecord update(PetWeightRecord record);

    /** 按ID删除 */
    void deleteById(Long id);

    /** 按ID查询 */
    PetWeightRecord getById(Long id);
}
