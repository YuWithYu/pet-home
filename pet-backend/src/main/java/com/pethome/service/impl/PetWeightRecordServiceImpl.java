package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.BusinessException;
import com.pethome.entity.PetWeightRecord;
import com.pethome.mapper.PetWeightRecordMapper;
import com.pethome.service.PetWeightRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 宠物体重记录 Service 实现
 *
 * <p>所有写操作均显式声明 @Transactional(rollbackFor = Exception.class)，
 * 保证非 RuntimeException 也能正确回滚（项目强制约束）。
 */
@Service
public class PetWeightRecordServiceImpl implements PetWeightRecordService {

    @Autowired
    private PetWeightRecordMapper mapper;

    @Override
    public List<PetWeightRecord> listByPetId(Integer petId) {
        QueryWrapper<PetWeightRecord> q = new QueryWrapper<>();
        q.eq("pet_id", petId).orderByAsc("record_date").orderByAsc("id");
        return mapper.selectList(q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PetWeightRecord add(PetWeightRecord record) {
        // 同一天重复称重：覆盖更新，避免曲线出现同日多点
        QueryWrapper<PetWeightRecord> sameDay = new QueryWrapper<>();
        sameDay.eq("pet_id", record.getPetId())
                .eq("record_date", record.getRecordDate());
        PetWeightRecord existing = mapper.selectOne(sameDay);
        if (existing != null) {
            existing.setWeight(record.getWeight());
            existing.setRemark(record.getRemark());
            mapper.updateById(existing);
            return existing;
        }
        mapper.insert(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PetWeightRecord update(PetWeightRecord record) {
        if (record == null || record.getId() == null) {
            throw new BusinessException("记录ID不能为空");
        }
        // 不允许修改归属字段与关联宠物
        PetWeightRecord current = mapper.selectById(record.getId());
        if (current == null) {
            throw new BusinessException("记录不存在");
        }
        current.setWeight(record.getWeight());
        current.setRecordDate(record.getRecordDate());
        current.setRemark(record.getRemark());
        mapper.updateById(current);
        return current;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PetWeightRecord getById(Long id) {
        if (id == null) {
            throw new BusinessException("记录ID不能为空");
        }
        return mapper.selectById(id);
    }
}
