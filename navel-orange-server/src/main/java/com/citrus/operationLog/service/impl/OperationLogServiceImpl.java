package com.citrus.operationLog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.common.entity.OperationLog;
import com.citrus.common.result.PageResult;
import com.citrus.operationLog.dto.OperationLogQueryDTO;
import com.citrus.operationLog.mapper.OperationLogMapper;
import com.citrus.operationLog.service.OperationLogService;
import com.citrus.operationLog.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    @Override
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

    @Override
    public PageResult<OperationLogVO> getLogList(OperationLogQueryDTO queryDTO) {
        // 1. 构建查询条件
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();

        // 操作类型过滤
        String action = queryDTO.getAction();
        if (StringUtils.hasText(action)) {
            wrapper.eq("action", action);
        }

        // 用户ID过滤
        Long userId = queryDTO.getUserId();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }

        // 2. 按创建时间降序排序
        wrapper.orderByDesc("created_at");

        // 3. 分页查询
        Page<OperationLog> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<OperationLog> logPage = operationLogMapper.selectPage(page, wrapper);

        // 4. 批量查询关联的用户名
        List<OperationLog> logList = logPage.getRecords();
        Set<Long> userIds = logList.stream()
                .map(OperationLog::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, String> usernameMap = userIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            User user = userMapper.selectById(id);
                            return user != null ? user.getUsername() : "未知用户";
                        }
                ));

        // 5. 转换为VO
        List<OperationLogVO> voList = logList.stream()
                .map(log -> convertToVO(log, usernameMap))
                .collect(Collectors.toList());

        // 6. 返回分页结果
        return PageResult.of(voList, logPage.getTotal(),
                queryDTO.getPage(), queryDTO.getSize());
    }

    /**
     * 转换为 OperationLogVO
     */
    private OperationLogVO convertToVO(OperationLog log, Map<Long, String> usernameMap) {
        return OperationLogVO.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUserId() != null ? usernameMap.getOrDefault(log.getUserId(), "未知用户") : null)
                .module(log.getModule())
                .action(log.getAction())
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
