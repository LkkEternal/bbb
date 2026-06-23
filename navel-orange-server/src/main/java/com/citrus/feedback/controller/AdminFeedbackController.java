package com.citrus.feedback.controller;

import com.citrus.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理端 - 用户反馈接口（占位实现）
 * TODO: 完整实现需要创建 feedback 表及相关实体
 */
@Tag(name = "M10-用户反馈")
@RestController
@RequestMapping("/api/admin/feedbacks")
public class AdminFeedbackController {

    /**
     * 反馈列表
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", Collections.emptyList());
        data.put("total", 0);
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    /**
     * 反馈详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(null);
    }

    /**
     * 审核反馈
     */
    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return Result.success(null);
    }

    /**
     * 导出训练集
     */
    @GetMapping("/export-dataset")
    public Result<Map<String, Object>> exportDataset() {
        return Result.success(null);
    }
}
