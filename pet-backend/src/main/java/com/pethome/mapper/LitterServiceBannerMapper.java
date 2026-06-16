package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.LitterServiceBanner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LitterServiceBannerMapper extends BaseMapper<LitterServiceBanner> {

    @Select("SELECT * FROM litter_service_banners WHERE position = #{position} AND status = 'active' AND is_deleted = false ORDER BY sort_order DESC LIMIT 1")
    LitterServiceBanner selectByPosition(String position);
}
