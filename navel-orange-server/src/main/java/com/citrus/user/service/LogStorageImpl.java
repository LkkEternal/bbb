package com.citrus.user.service;

import com.citrus.common.aspect.OperationLogAspect;
import com.citrus.user.entity.OperationLog;
import com.citrus.user.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 日志存储实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogStorageImpl implements OperationLogAspect.LogStorage {

    private final OperationLogMapper operationLogMapper;

    @Override
    @Async
    public void save(OperationLogAspect.LogRecord record) {
        try {
            OperationLog entity = new OperationLog();
            entity.setUserId(record.getUserId());
            entity.setModule(record.getModule());
            entity.setAction(record.getAction());
            entity.setResourceType(record.getResourceType());
            entity.setResourceId(record.getResourceId());
            entity.setDescription(record.getDescription());
            entity.setDetail(record.getDetail());
            entity.setIpAddress(record.getIpAddress());
            entity.setUserAgent(record.getUserAgent());
            entity.setCreatedAt(record.getCreatedAt());

            operationLogMapper.insert(entity);
            log.debug("操作日志保存成功: {}", record.getDescription());
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
