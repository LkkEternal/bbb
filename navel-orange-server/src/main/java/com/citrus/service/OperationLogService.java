package com.citrus.service;

import com.citrus.common.entity.OperationLog;
import com.citrus.common.result.PageResult;
import com.citrus.dto.OperationLogQueryDTO;
import com.citrus.vo.OperationLogVO;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {

    /**
     * 异步保存操作日志
     *
     * @param operationLog 日志对象
     */
    void saveLog(OperationLog operationLog);

    /**
     * 分页查询操作日志列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<OperationLogVO> getLogList(OperationLogQueryDTO queryDTO);
}
