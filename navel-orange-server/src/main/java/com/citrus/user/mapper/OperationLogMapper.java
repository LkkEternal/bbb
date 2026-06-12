package com.citrus.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.user.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
