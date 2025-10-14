package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Consultation;
import com.pethome.mapper.ConsultationMapper;
import com.pethome.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsultationServiceImpl implements ConsultationService {

    @Autowired
    private ConsultationMapper consultationMapper;

    @Override
    public IPage<Consultation> getConsultationList(Page<Consultation> page) {
        return consultationMapper.selectPage(page, null);
    }

    @Override
    public Consultation createConsultation(Consultation consultation) {
        consultationMapper.insert(consultation);
        return consultation;
    }

    @Override
    public Consultation updateConsultation(Consultation consultation) {
        consultationMapper.updateById(consultation);
        return consultation;
    }

    @Override
    public boolean deleteConsultation(Long id) {
        return consultationMapper.deleteById(id) > 0;
    }

    @Override
    public Consultation getConsultationById(Long id) {
        return consultationMapper.selectById(id);
    }
}
