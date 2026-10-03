package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.GroomingServiceBanner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GroomingServiceBannerMapper extends BaseMapper<GroomingServiceBanner> {

    @Select("SELECT * FROM grooming_service_banners WHERE position = #{position} AND status = 'active' AND is_deleted = false ORDER BY sort_order DESC LIMIT 1")
    GroomingServiceBanner selectByPosition(String position);
}


