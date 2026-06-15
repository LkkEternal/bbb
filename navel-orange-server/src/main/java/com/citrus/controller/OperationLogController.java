package com.citrus.controller;

import com.citrus.common.result.PageResult;
import com.citrus.common.result.Result;
import com.citrus.dto.OperationLogQueryDTO;
import com.citrus.service.OperationLogService;
import com.citrus.vo.OperationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员 - 操作日志控制器
 */
@Tag(name = "操作日志", description = "管理员操作日志接口")
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 操作日志列表
     */
    @Operation(summary = "操作日志列表", description = "分页查询操作日志列表，支持按操作类型和用户ID过滤")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<OperationLogVO>> getLogList(OperationLogQueryDTO queryDTO) {
        PageResult<OperationLogVO> result = operationLogService.getLogList(queryDTO);
        return Result.success(result);
    }
}
