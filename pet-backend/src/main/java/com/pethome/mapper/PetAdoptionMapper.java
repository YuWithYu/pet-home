package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.PetAdoption;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetAdoptionMapper extends BaseMapper<PetAdoption> {
}