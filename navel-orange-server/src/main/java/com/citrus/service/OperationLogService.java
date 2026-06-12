package com.citrus.service;

import com.citrus.common.entity.OperationLog;
import com.citrus.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务
 * 异步保存操作日志，避免影响主业务性能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 异步保存操作日志
     *
     * @param operationLog 日志对象
     */
    @Async
    public void saveLog(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
            log.debug("操作日志保存成功: module={}, action={}", 
                    operationLog.getModule(), operationLog.getAction());
        } catch (Exception e) {
            log.error("操作日志保存失败: module={}, action={}, error={}", 
                    operationLog.getModule(), operationLog.getAction(), e.getMessage());
        }
    }
}
