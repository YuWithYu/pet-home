package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.MedicalServiceBanner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MedicalServiceBannerMapper extends BaseMapper<MedicalServiceBanner> {
    
    @Select("SELECT * FROM medical_service_banners WHERE position = #{position} AND status = 'active' AND is_deleted = false ORDER BY sort_order DESC LIMIT 1")
    MedicalServiceBanner selectByPosition(String position);
}
