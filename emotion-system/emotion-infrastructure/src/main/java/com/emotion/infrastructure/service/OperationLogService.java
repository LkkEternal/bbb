package com.emotion.infrastructure.service;

import com.emotion.infrastructure.entity.OperationLogEntity;
import com.emotion.infrastructure.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Async
    public void saveLog(OperationLogEntity entity) {
        try {
            log.info("[OperationLog] 异步保存操作日志: module={}, action={}, userId={}",
                    entity.getModule(), entity.getAction(), entity.getUserId());
            operationLogMapper.insert(entity);
            log.info("[OperationLog] 操作日志保存成功");
        } catch (Exception e) {
            log.error("[OperationLog] 操作日志保存失败", e);
        }
    }
}
