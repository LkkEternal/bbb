package com.emotion.user.controller;

import com.emotion.common.annotation.OperationLog;
import com.emotion.common.response.Result;
import com.emotion.user.dto.ChangePasswordRequest;
import com.emotion.user.dto.NotificationSettingsRequest;
import com.emotion.user.dto.UserDTO;
import com.emotion.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserDTO> getProfile() {
        Long userId = getCurrentUserId();
        UserDTO response = userService.getCurrentUser(userId);
        return Result.success(response);
    }

    @PutMapping("/password")
    @OperationLog(module = "user", action = "update", description = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId();
        userService.changePassword(userId, request);
        return Result.success("密码修改成功，请重新登录", null);
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        String url = userService.uploadAvatar(userId, file);
        return Result.success("上传成功", url);
    }

    @PutMapping("/notification-settings")
    public Result<UserDTO> updateNotificationSettings(@RequestBody NotificationSettingsRequest request) {
        Long userId = getCurrentUserId();
        UserDTO response = userService.updateNotificationSettings(userId, request);
        return Result.success("设置成功", response);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
