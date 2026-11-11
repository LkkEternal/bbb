package com.emotion.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.infrastructure.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}
