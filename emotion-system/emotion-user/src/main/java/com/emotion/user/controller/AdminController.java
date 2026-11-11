package com.emotion.user.controller;

import com.emotion.common.annotation.OperationLog;
import com.emotion.common.response.Result;
import com.emotion.user.dto.*;
import com.emotion.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public Result<UserPageResponse> getUserList(UserPageRequest request) {
        UserPageResponse response = userService.getUserList(request);
        return Result.success(response);
    }

    @GetMapping("/users/{id}")
    public Result<UserDetailResponse> getUserDetail(@PathVariable Long id) {
        UserDetailResponse response = userService.getUserDetail(id);
        return Result.success(response);
    }

    @OperationLog(module = "user", action = "update", description = "更新用户状态")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                         @Valid @RequestBody UpdateStatusRequest request) {
        userService.updateUserStatus(id, request.getStatus());
        return Result.success("更新成功", null);
    }

    @GetMapping("/logs")
    public Result<OperationLogPageResponse> getOperationLogs(OperationLogPageRequest request) {
        OperationLogPageResponse response = userService.getOperationLogs(request);
        return Result.success(response);
    }
}
