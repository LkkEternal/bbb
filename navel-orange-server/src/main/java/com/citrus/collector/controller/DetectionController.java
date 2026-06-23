package com.citrus.collector.controller;

import com.citrus.collector.service.DetectionQueryService;
import com.citrus.collector.vo.DetectionListVO;
import com.citrus.collector.vo.DetectionStatsVO;
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
 * 识别记录查询控制器
 */
@RestController
@RequestMapping("/api/detections")
@RequiredArgsConstructor
@Tag(name = "识别记录", description = "病害识别记录查询接口")
public class DetectionController {

    private final DetectionQueryService detectionQueryService;
    private final SecurityUtil securityUtil;

    @GetMapping
    @Operation(summary = "识别记录列表", description = "分页查询病害识别记录，支持按果园、病害编码、日期范围筛选")
    public Result<PageResult<DetectionListVO>> getDetectionList(
            @Parameter(description = "果园ID（可选，管理员不传查所有，农户不传查关联果园）")
            @RequestParam(value = "orchardId", required = false) Long orchardId,
            @Parameter(description = "病害编码（如 S4、S14）")
            @RequestParam(value = "diseaseCode", required = false) String diseaseCode,
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
        PageResult<DetectionListVO> result = detectionQueryService.getDetectionList(
                orchardId, diseaseCode, startDate, endDate, pageQuery, userId);
        return Result.success(result);
    }

    @GetMapping("/stats")
    @Operation(summary = "识别统计", description = "获取病害识别统计数据，包括病害分布和每日趋势")
    public Result<DetectionStatsVO> getDetectionStats(
            @Parameter(description = "果园ID（可选，管理员不传统计所有，农户不传统计关联果园）")
            @RequestParam(value = "orchardId", required = false) Long orchardId,
            @Parameter(description = "开始日期，格式：yyyy-MM-dd", required = true)
            @RequestParam(value = "startDate") String startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd", required = true)
            @RequestParam(value = "endDate") String endDate) {

        Long userId = securityUtil.getCurrentUserId();
        DetectionStatsVO result = detectionQueryService.getDetectionStats(
                orchardId, startDate, endDate, userId);
        return Result.success(result);
    }
}
