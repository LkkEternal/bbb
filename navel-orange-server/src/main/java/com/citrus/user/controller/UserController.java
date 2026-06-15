package com.citrus.user.controller;

import com.citrus.common.result.Result;
import com.citrus.user.dto.ChangePasswordDTO;
import com.citrus.user.dto.NotificationSettingsDTO;
import com.citrus.user.service.UserService;
import com.citrus.user.vo.NotificationSettingsVO;
import com.citrus.user.vo.UserProfileVO;
import com.citrus.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 普通用户控制器
 */
@Tag(name = "用户", description = "普通用户接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    /**
     * 查看个人信息
     */
    @Operation(summary = "查看个人信息", description = "登录用户查看自己的个人信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> getCurrentUserProfile() {
        Long userId = securityUtil.getCurrentUserId();
        UserProfileVO profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "登录用户修改自己的密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        userService.changePassword(userId, dto);
        return Result.success();
    }

    /**
     * 通知设置
     */
    @Operation(summary = "通知设置", description = "登录用户设置通知偏好")
    @PutMapping("/notification-settings")
    public Result<NotificationSettingsVO> updateNotificationSettings(@RequestBody NotificationSettingsDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        NotificationSettingsVO settings = userService.updateNotificationSettings(userId, dto);
        return Result.success(settings);
    }
}
