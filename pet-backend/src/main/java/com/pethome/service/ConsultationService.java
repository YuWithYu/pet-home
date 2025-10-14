package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Consultation;

public interface ConsultationService {
    IPage<Consultation> getConsultationList(Page<Consultation> page);
    Consultation createConsultation(Consultation consultation);
    Consultation updateConsultation(Consultation consultation);
    boolean deleteConsultation(Long id);
    Consultation getConsultationById(Long id);
}
