package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.PetMedicalRecord;
import com.pethome.mapper.PetMedicalRecordMapper;
import com.pethome.service.PetMedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetMedicalRecordServiceImpl implements PetMedicalRecordService {

    @Autowired
    private PetMedicalRecordMapper mapper;

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
    public PetMedicalRecord add(PetMedicalRecord record) {
        mapper.insert(record);
        return record;
    }

    @Override
    public PetMedicalRecord getById(Long id) {
        return mapper.selectById(id);
    }
}
