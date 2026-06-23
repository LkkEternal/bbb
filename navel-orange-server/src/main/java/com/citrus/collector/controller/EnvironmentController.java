package com.citrus.collector.controller;

import com.citrus.collector.service.EnvironmentService;
import com.citrus.collector.vo.EnvironmentListVO;
import com.citrus.collector.vo.EnvironmentTrendVO;
import com.citrus.common.result.PageQuery;
import com.citrus.common.result.PageResult;
import com.citrus.common.result.Result;
import com.citrus.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 环境数据控制器
 */
@RestController
@RequestMapping("/api/environment")
@RequiredArgsConstructor
@Tag(name = "环境数据", description = "环境数据查询接口")
public class EnvironmentController {

    private final EnvironmentService environmentService;
    private final SecurityUtil securityUtil;

    /**
     * 环境数据列表（分页）
     * 仅与果园相关的用户才能访问
     */
    @GetMapping
    @Operation(summary = "环境数据列表", description = "获取指定果园的环境数据列表，仅果园关联用户可访问")
    public Result<PageResult<EnvironmentListVO>> getEnvironmentList(
            @Parameter(description = "果园ID", required = true)
            @RequestParam("orchardId") Long orchardId,
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam(value = "startDate", required = false) String startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam(value = "endDate", required = false) String endDate,
            @Parameter(description = "页码")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(value = "size", defaultValue = "20") Integer size) {

        Long userId = securityUtil.getCurrentUserId();
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        PageResult<EnvironmentListVO> result = environmentService.getEnvironmentList(
                orchardId, startDate, endDate, pageQuery, userId);
        return Result.success(result);
    }

    /**
     * 环境趋势图
     * 仅与果园相关的用户才能访问
     */
    @GetMapping("/trend")
    @Operation(summary = "环境趋势图", description = "获取指定果园的环境趋势数据，仅果园关联用户可访问")
    public Result<EnvironmentTrendVO> getEnvironmentTrend(
            @Parameter(description = "果园ID", required = true)
            @RequestParam("orchardId") Long orchardId,
            @Parameter(description = "查询天数，默认7，可选：7/15/30")
            @RequestParam(value = "days", defaultValue = "7") Integer days) {

        Long userId = securityUtil.getCurrentUserId();
        EnvironmentTrendVO result = environmentService.getEnvironmentTrend(orchardId, days, userId);
        return Result.success(result);
    }
}
