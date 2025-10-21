package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.AdoptionService;
import com.pethome.mapper.AdoptionServiceMapper;
import com.pethome.service.AdoptionServiceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdoptionServiceServiceImpl extends ServiceImpl<AdoptionServiceMapper, AdoptionService> implements AdoptionServiceService {

    @Override
    public List<AdoptionService> getAdoptionServiceList() {
        QueryWrapper<AdoptionService> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("sort_order").orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public AdoptionService getAdoptionServiceById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean createAdoptionService(AdoptionService service) {
        return this.save(service);
    }

    @Override
    public boolean updateAdoptionService(AdoptionService service) {
        return this.updateById(service);
    }

    @Override
    public boolean deleteAdoptionService(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean updateAdoptionServiceStatus(Long id, String status) {
        AdoptionService service = this.getById(id);
        if (service != null) {
            service.setStatus(status);
            return this.updateById(service);
        }
        return false;
    }
}
