package com.citrus.operationLog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.common.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
