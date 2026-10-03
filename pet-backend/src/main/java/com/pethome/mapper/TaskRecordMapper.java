package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.TaskRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务记录Mapper
 */
@Mapper
public interface TaskRecordMapper extends BaseMapper<TaskRecord> {
}

