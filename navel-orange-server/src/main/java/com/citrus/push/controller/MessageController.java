package com.citrus.push.controller;

import com.citrus.common.result.PageResult;
import com.citrus.common.result.Result;
import com.citrus.push.service.MessageService;
import com.citrus.push.vo.MessageDetailVO;
import com.citrus.push.vo.MessageListVO;
import com.citrus.push.vo.UnreadCountVO;
import com.citrus.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "消息推送", description = "消息推送相关接口")
public class MessageController {

    private final MessageService messageService;
    private final SecurityUtil securityUtil;

    @GetMapping//请求映射
    @Operation(summary = "我的消息列表", description = "分页查询消息列表，支持按预警等级和已读状态筛选")//Swagger/OpenAPI 注解，用于生成 API 文档
    public Result<PageResult<MessageListVO>> getMessageList(     //返回类型：统一响应格式，包含分页数据
            @Parameter(description = "预警等级：1=L1 / 2=L2 / 3=L3") //@Parameter注解，Swagger/OpenAPI 注解，用于生成 API 文档
            @RequestParam(value = "level", required = false) Integer level,//表示从请求URL中查询参数中获取值
            @Parameter(description = "已读状态：0=未读 / 1=已读")
            @RequestParam(value = "readStatus", required = false) Integer readStatus,
            @Parameter(description = "页码")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(value = "size", defaultValue = "20") Integer size) {

        Long userId = securityUtil.getCurrentUserId();
        PageResult<MessageListVO> result = messageService.getMessageList(level, readStatus, page, size, userId);
        return Result.success(result);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读数量", description = "获取未读消息数量")
    public Result<UnreadCountVO> getUnreadCount() {
        Long userId = securityUtil.getCurrentUserId();
        UnreadCountVO result = messageService.getUnreadCount(userId);
        return Result.success(result);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "标记单条消息为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID")
            @PathVariable("id") Long id) {
        Long userId = securityUtil.getCurrentUserId();
        messageService.markAsRead(id, userId);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部已读", description = "标记所有消息为已读")
    public Result<Object> markAllAsRead() {
        Long userId = securityUtil.getCurrentUserId();
        int markedCount = messageService.markAllAsRead(userId);
        return Result.success(java.util.Map.of("markedCount", markedCount));
    }

    @GetMapping("/{id}")
    @Operation(summary = "消息详情", description = "查看消息详情")
    public Result<MessageDetailVO> getMessageDetail(
            @Parameter(description = "消息ID")
            @PathVariable("id") Long id) {
        Long userId = securityUtil.getCurrentUserId();
        MessageDetailVO result = messageService.getMessageDetail(id, userId);
        return Result.success(result);
    }
}
