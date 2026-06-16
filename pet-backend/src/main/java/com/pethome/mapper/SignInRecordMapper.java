package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.SignInRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 签到记录Mapper
 */
@Mapper
public interface SignInRecordMapper extends BaseMapper<SignInRecord> {
}

