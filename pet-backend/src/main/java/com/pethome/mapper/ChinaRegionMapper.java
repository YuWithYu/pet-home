package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.ChinaRegion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChinaRegionMapper extends BaseMapper<ChinaRegion> {
    
    @Select("SELECT * FROM china_region WHERE level = 1 AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<ChinaRegion> getProvinces();
    
    @Select("SELECT * FROM china_region WHERE level = 2 AND parent_code = #{parentCode} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<ChinaRegion> getCitiesByParentCode(String parentCode);
    
    @Select("SELECT * FROM china_region WHERE level = 3 AND parent_code = #{parentCode} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<ChinaRegion> getDistrictsByParentCode(String parentCode);
    
    @Select("SELECT * FROM china_region WHERE code = #{code} AND status = 1")
    ChinaRegion getByCode(String code);
    
    @Select("SELECT * FROM china_region WHERE parent_code = #{parentCode} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<ChinaRegion> getByParentCode(String parentCode);
}
