package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.BusinessException;
import com.pethome.entity.Pet;
import com.pethome.entity.PetMedicalRecord;
import com.pethome.mapper.PetMapper;
import com.pethome.mapper.PetMedicalRecordMapper;
import com.pethome.service.PetMedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 宠物就医记录 Service 实现
 *
 * <p>所有写操作均显式声明 @Transactional(rollbackFor = Exception.class)，
 * 保证非 RuntimeException 也能正确回滚（项目强制约束）。
 */
@Service
public class PetMedicalRecordServiceImpl implements PetMedicalRecordService {

    @Autowired
    private PetMedicalRecordMapper mapper;

    @Autowired
    private PetMapper petMapper;

    @Override
    public List<PetMedicalRecord> listByPetId(Integer petId) {
        QueryWrapper<PetMedicalRecord> q = new QueryWrapper<>();
        q.eq("pet_id", petId).orderByDesc("record_date").orderByDesc("id");
        return mapper.selectList(q);
    }

    @Override
    public List<PetMedicalRecord> listByAppointmentId(Long hospitalAppointmentId) {
        QueryWrapper<PetMedicalRecord> q = new QueryWrapper<>();
        q.eq("hospital_appointment_id", hospitalAppointmentId).orderByDesc("record_date");
        return mapper.selectList(q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PetMedicalRecord add(PetMedicalRecord record) {
        mapper.insert(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PetMedicalRecord update(PetMedicalRecord record) {
        if (record.getId() == null) {
            throw new BusinessException("记录ID不能为空");
        }
        mapper.updateById(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public PetMedicalRecord getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<PetMedicalRecord> listUpcoming(Integer petId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        QueryWrapper<PetMedicalRecord> q = new QueryWrapper<>();
        q.eq("pet_id", petId)
                .in("record_type", "vaccination", "deworming")
                .isNotNull("next_due_date")
                .le("next_due_date", deadline)
                .orderByAsc("next_due_date");
        return filterActive(mapper.selectList(q), today);
    }

    @Override
    public List<PetMedicalRecord> listUpcomingByUser(Long userId, int days) {
        List<Pet> pets = petMapper.selectList(
                new QueryWrapper<Pet>().eq("user_id", userId));
        if (pets.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> petIds = pets.stream().map(Pet::getId).collect(Collectors.toList());
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        QueryWrapper<PetMedicalRecord> q = new QueryWrapper<>();
        q.in("pet_id", petIds)
                .in("record_type", "vaccination", "deworming")
                .isNotNull("next_due_date")
                .le("next_due_date", deadline)
                .orderByAsc("next_due_date");
        return filterActive(mapper.selectList(q), today);
    }

    /**
     * 过滤已完成的记录：若同宠物同类型存在更晚的接种/驱虫记录，说明本条已被覆盖，不再提醒。
     *
     * <p>性能优化：旧实现对每条记录发起 1 次 SELECT COUNT（N+1 问题，提醒窗口 365 天或多宠物时
     * 会产生数十次数据库往返）。新实现仅对同一批 petId+recordType 做一次批量查询取最新 record_date，
     * 再用 Stream 分组比对，数据库往返次数固定为 2 次。
     */
    private List<PetMedicalRecord> filterActive(List<PetMedicalRecord> records, LocalDate today) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        // 先排除 next_due_date 为 null 的记录
        List<PetMedicalRecord> candidates = records.stream()
                .filter(r -> r.getNextDueDate() != null)
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return candidates;
        }

        // 按 (petId, recordType) 分组，收集候选记录里需要核查的分组键
        List<Object[]> groupKeys = candidates.stream()
                .map(r -> new Object[]{r.getPetId(), r.getRecordType()})
                .distinct()
                .collect(Collectors.toList());

        // 为每个分组键查询该组中「record_date 最新」的一条（无论是否在候选集中）
        // 采用批量 OR 条件一次查询，再在内存里按分组聚合
        QueryWrapper<PetMedicalRecord> newestQ = new QueryWrapper<>();
        boolean first = true;
        for (Object[] key : groupKeys) {
            if (first) {
                newestQ.and(w -> w.eq("pet_id", key[0]).eq("record_type", key[1]));
                first = false;
            } else {
                newestQ.or(w -> w.eq("pet_id", key[0]).eq("record_type", key[1]));
            }
        }
        List<PetMedicalRecord> allInGroups = mapper.selectList(newestQ);

        // key: petId|recordType  -> value: 该组最新 record_date
        Map<String, LocalDate> newestByGroup = allInGroups.stream()
                .filter(r -> r.getPetId() != null && r.getRecordType() != null && r.getRecordDate() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getPetId() + "|" + r.getRecordType(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(PetMedicalRecord::getRecordDate)),
                                opt -> opt.map(PetMedicalRecord::getRecordDate).orElse(null)
                        )
                ));

        List<PetMedicalRecord> result = new ArrayList<>(candidates.size());
        for (PetMedicalRecord r : candidates) {
            String key = r.getPetId() + "|" + r.getRecordType();
            LocalDate newestDate = newestByGroup.get(key);
            // 仅当本条记录的 record_date 与该组最新 record_date 相同，才认定为尚未被后续记录覆盖
            if (newestDate != null && r.getRecordDate() != null
                    && Objects.equals(r.getRecordDate(), newestDate)) {
                result.add(r);
            }
        }
        return result;
    }
}
