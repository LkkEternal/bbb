package com.emotion.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emotion.admin.dto.AlertMessageDTO;
import com.emotion.admin.dto.AlertMessagePageResponse;
import com.emotion.admin.dto.AlertSummaryResponse;
import com.emotion.admin.entity.AlertMessage;
import com.emotion.admin.mapper.AlertMessageMapper;
import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import com.emotion.common.response.Result;
import com.emotion.user.entity.User;
import com.emotion.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 预警消息控制器
 * <p>
 * 普通用户仅能查看/操作自己所在地区（location_id）触发的预警消息，
 * 管理员可查看/操作全部。
 */
@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertMessageController {

    private static final int SUMMARY_SIZE = 3;

    private final AlertMessageMapper alertMessageMapper;
    private final UserMapper userMapper;

    /**
     * 分页查询预警消息
     */
    @GetMapping("/messages")
    public Result<AlertMessagePageResponse> getMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        User current = currentUser();
        LambdaQueryWrapper<AlertMessage> wrapper = baseWrapper(current);
        wrapper.orderByDesc(AlertMessage::getCreatedAt).orderByDesc(AlertMessage::getId);

        Page<AlertMessage> result = alertMessageMapper.selectPage(new Page<>(page, size), wrapper);

        AlertMessagePageResponse response = new AlertMessagePageResponse();
        response.setTotal(result.getTotal());
        response.setPage((int) result.getCurrent());
        response.setSize((int) result.getSize());
        response.setPages((int) result.getPages());
        response.setRecords(result.getRecords().stream().map(this::toDTO).collect(Collectors.toList()));
        return Result.success(response);
    }

    /**
     * 预警摘要：总数 + 最新几条（首页「全部预警」卡回显）
     */
    @GetMapping("/messages/summary")
    public Result<AlertSummaryResponse> getSummary() {
        User current = currentUser();
        Long total = alertMessageMapper.selectCount(baseWrapper(current));

        LambdaQueryWrapper<AlertMessage> recentWrapper = baseWrapper(current);
        recentWrapper.orderByDesc(AlertMessage::getCreatedAt).orderByDesc(AlertMessage::getId)
                .last("LIMIT " + SUMMARY_SIZE);
        List<AlertMessage> recent = alertMessageMapper.selectList(recentWrapper);

        AlertSummaryResponse response = new AlertSummaryResponse();
        response.setTotal(total);
        response.setRecent(recent.stream().map(this::toDTO).collect(Collectors.toList()));
        return Result.success(response);
    }

    /**
     * 标记预警消息为已读
     */
    @PutMapping("/messages/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        AlertMessage message = alertMessageMapper.selectById(id);
        if (message == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "预警消息不存在");
        }
        User current = currentUser();
        if (!isAdmin(current) && !Objects.equals(message.getUserId(), current.getId())
                && !(message.getUserId() == null && Objects.equals(message.getLocation(), current.getLocationId()))) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权操作该预警消息");
        }
        alertMessageMapper.update(null, new LambdaUpdateWrapper<AlertMessage>()
                .eq(AlertMessage::getId, id)
                .set(AlertMessage::getStatus, 1));
        return Result.success("已标记为已读", null);
    }

    /**
     * 普通用户按"自己user_id的情绪预警 OR 自己地区的天气预警"过滤，管理员查全部
     */
    private LambdaQueryWrapper<AlertMessage> baseWrapper(User current) {
        LambdaQueryWrapper<AlertMessage> wrapper = new LambdaQueryWrapper<>();
        if (!isAdmin(current)) {
            wrapper.and(w -> w
                    .eq(AlertMessage::getUserId, current.getId())
                    .or(o -> o.isNull(AlertMessage::getUserId)
                            .eq(AlertMessage::getLocation, current.getLocationId())));
        }
        return wrapper;
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null && user.getRole() == 2;
    }

    private AlertMessageDTO toDTO(AlertMessage message) {
        AlertMessageDTO dto = new AlertMessageDTO();
        dto.setId(message.getId());
        dto.setRuleId(message.getRuleId());
        dto.setRuleName(message.getRuleName());
        dto.setLocation(message.getLocation());
        dto.setCityName(message.getCityName());
        dto.setUserId(message.getUserId());
        dto.setFxDateStart(message.getFxDateStart());
        dto.setFxDateEnd(message.getFxDateEnd());
        dto.setAlertContent(message.getAlertContent());
        dto.setActive(message.getActive());
        dto.setStatus(message.getStatus());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
