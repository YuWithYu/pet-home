package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.AdoptionService;

import java.util.List;

public interface AdoptionServiceService extends IService<AdoptionService> {

    List<AdoptionService> getAdoptionServiceList();

    AdoptionService getAdoptionServiceById(Long id);

    boolean createAdoptionService(AdoptionService service);

    boolean updateAdoptionService(AdoptionService service);

    boolean deleteAdoptionService(Long id);

    boolean updateAdoptionServiceStatus(Long id, String status);
}
