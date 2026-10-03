package com.pethome.service;

import com.pethome.entity.ChinaRegion;
import java.util.List;

public interface ChinaRegionService {
    
    /**
     * 获取所有省份
     */
    List<ChinaRegion> getProvinces();
    
    /**
     * 根据父级代码获取城市
     */
    List<ChinaRegion> getCitiesByParentCode(String parentCode);
    
    /**
     * 根据父级代码获取区县
     */
    List<ChinaRegion> getDistrictsByParentCode(String parentCode);
    
    /**
     * 根据代码获取地区信息
     */
    ChinaRegion getByCode(String code);
    
    /**
     * 根据父级代码获取子级地区
     */
    List<ChinaRegion> getByParentCode(String parentCode);
}
