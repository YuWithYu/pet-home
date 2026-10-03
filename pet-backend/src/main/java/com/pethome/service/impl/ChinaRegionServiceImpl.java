package com.pethome.service.impl;

import com.pethome.entity.ChinaRegion;
import com.pethome.mapper.ChinaRegionMapper;
import com.pethome.service.ChinaRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChinaRegionServiceImpl implements ChinaRegionService {
    
    @Autowired
    private ChinaRegionMapper chinaRegionMapper;
    
    @Override
    public List<ChinaRegion> getProvinces() {
        return chinaRegionMapper.getProvinces();
    }
    
    @Override
    public List<ChinaRegion> getCitiesByParentCode(String parentCode) {
        return chinaRegionMapper.getCitiesByParentCode(parentCode);
    }
    
    @Override
    public List<ChinaRegion> getDistrictsByParentCode(String parentCode) {
        return chinaRegionMapper.getDistrictsByParentCode(parentCode);
    }
    
    @Override
    public ChinaRegion getByCode(String code) {
        return chinaRegionMapper.getByCode(code);
    }
    
    @Override
    public List<ChinaRegion> getByParentCode(String parentCode) {
        return chinaRegionMapper.getByParentCode(parentCode);
    }
}
