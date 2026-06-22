package com.citrus.alert.controller;

import com.citrus.alert.dto.UpdateRuleDTO;
import com.citrus.alert.service.AlertRuleService;
import com.citrus.alert.vo.RuleListVO;
import com.citrus.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员规则管理控制器
 *
 * 提供预警规则的管理接口（仅管理员可访问）
 */
@RestController
@RequestMapping("/api/admin/rules")
@RequiredArgsConstructor
@Tag(name = "预警规则管理", description = "管理员规则管理接口")
public class AdminRuleController {

    private final AlertRuleService alertRuleService;

    /**
     * 获取规则列表
     */
    @GetMapping
    @Operation(summary = "规则列表", description = "查看所有预警规则")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RuleListVO>> getRuleList() {
        List<RuleListVO> rules = alertRuleService.getRuleList();
        return Result.success(rules);
    }

    /**
     * 更新规则阈值
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新规则阈值", description = "修改规则的阈值参数")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateRule(
            @Parameter(description = "规则ID", required = true)
            @PathVariable("id") Integer id,
            @RequestBody UpdateRuleDTO dto) {
        alertRuleService.updateRule(id, dto);
        return Result.success();
    }

    /**
     * 切换规则启用状态
     */
    @PutMapping("/{id}/toggle")
    @Operation(summary = "启用/禁用规则", description = "切换规则的启用状态")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Boolean>> toggleRule(
            @Parameter(description = "规则ID", required = true)
            @PathVariable("id") Integer id) {
        Map<String, Boolean> result = alertRuleService.toggleRule(id);
        return Result.success(result);
    }
}
