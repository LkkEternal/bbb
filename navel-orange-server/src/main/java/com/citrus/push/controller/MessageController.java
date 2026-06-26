package com.citrus.push.controller;

import com.citrus.auth.model.LoginUser;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.PageResult;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
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

import java.util.Map;

/**
 * 消息推送控制器
 *
 * 提供消息相关的RESTful API接口
 * 所有接口都需要JWT认证（🔒 需登录）
 *
 * 接口列表：
 * - GET  /api/messages          获取消息列表（分页）
 * - GET  /api/messages/unread-count  获取未读消息数量
 * - PUT  /api/messages/{id}/read     标记单条消息为已读
 * - PUT  /api/messages/read-all      标记所有消息为已读
 * - GET  /api/messages/{id}          获取消息详情
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "消息推送", description = "消息推送相关接口")
public class MessageController {

    private final MessageService messageService;
    private final SecurityUtil securityUtil;

    /**
     * 获取我的消息列表
     *
     * @param level      预警等级筛选（1=L1 / 2=L2 / 3=L3），可选
     * @param readStatus 已读状态筛选（0=未读 / 1=已读），可选
     * @param page       页码，默认1
     * @param size       每页数量，默认20
     * @return 分页消息列表
     */
    @GetMapping
    @Operation(summary = "我的消息列表", description = "分页查询消息列表，支持按预警等级和已读状态筛选。管理员查看所有消息")
    public Result<PageResult<MessageListVO>> getMessageList(
            @Parameter(description = "预警等级：1=L1 / 2=L2 / 3=L3")
            @RequestParam(value = "level", required = false) Integer level,
            @Parameter(description = "已读状态：0=未读 / 1=已读")
            @RequestParam(value = "readStatus", required = false) Integer readStatus,
            @Parameter(description = "页码")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(value = "size", defaultValue = "20") Integer size) {

        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 管理员查看所有消息（userId 传 null），普通用户只看自己的
        PageResult<MessageListVO> result = messageService.getMessageList(
                level, readStatus, page, size, isAdmin ? null : userId);
        return Result.success(result);
    }

    /**
     * 获取未读消息数量
     *
     * @return 未读数量，包含总数和按等级分类的数量（L1/L2/L3）
     */
    @GetMapping("/unread-count")
    @Operation(summary = "未读数量", description = "获取未读消息数量。管理员统计所有用户的未读消息")
    public Result<UnreadCountVO> getUnreadCount() {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        UnreadCountVO result = messageService.getUnreadCount(isAdmin ? null : userId);
        return Result.success(result);
    }

    /**
     * 标记单条消息为已读
     *
     * @param id 消息ID（路径参数）
     * @return 操作结果
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "标记单条消息为已读（管理员无权限）")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID")
            @PathVariable("id") Long id) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "管理员无权标记消息已读");
        }
        messageService.markAsRead(id, loginUser.getUserId());
        return Result.success();
    }

    /**
     * 标记单条消息为未读
     *
     * @param id 消息ID（路径参数）
     * @return 操作结果
     */
    @PutMapping("/{id}/unread")
    @Operation(summary = "标记未读", description = "标记单条消息为未读（管理员无权限）")
    public Result<Void> markAsUnread(
            @Parameter(description = "消息ID")
            @PathVariable("id") Long id) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "管理员无权标记消息未读");
        }
        messageService.markAsUnread(id, loginUser.getUserId());
        return Result.success();
    }

    /**
     * 标记所有消息为已读
     *
     * @return 包含标记数量的响应
     */
    @PutMapping("/read-all")
    @Operation(summary = "全部已读", description = "标记所有消息为已读（管理员无权限）")
    public Result<Map<String, Integer>> markAllAsRead() {
        LoginUser loginUser = securityUtil.getCurrentUser();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "管理员无权标记消息已读");
        }
        int markedCount = messageService.markAllAsRead(loginUser.getUserId());
        return Result.success(Map.of("markedCount", markedCount));
    }

    /**
     * 获取消息详情
     *
     * 注意：默认会自动将消息标记为已读，可通过 autoRead=false 禁用
     *
     * @param id       消息ID（路径参数）
     * @param autoRead 是否自动标记已读，默认 true
     * @return 消息详情，包含完整内容和防治建议
     */
    @GetMapping("/{id}")
    @Operation(summary = "消息详情", description = "查看消息详情。管理员可查看任意消息但不自动标记已读")
    public Result<MessageDetailVO> getMessageDetail(
            @Parameter(description = "消息ID")
            @PathVariable("id") Long id,
            @Parameter(description = "是否自动标记已读，默认 true")
            @RequestParam(value = "autoRead", defaultValue = "true") Boolean autoRead) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 管理员始终不自动标记已读
        if (isAdmin) {
            autoRead = false;
        }

        // 管理员 userId 传 null 表示不限制归属
        MessageDetailVO result = messageService.getMessageDetail(id, isAdmin ? null : userId, autoRead);
        return Result.success(result);
    }
}
