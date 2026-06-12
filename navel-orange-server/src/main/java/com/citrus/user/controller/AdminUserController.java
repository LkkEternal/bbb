package com.citrus.user.controller;

import com.citrus.common.result.PageResult;
import com.citrus.common.result.Result;
import com.citrus.user.dto.UserQueryDTO;
import com.citrus.user.service.UserService;
import com.citrus.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 - 用户管理控制器
 */
@Tag(name = "用户管理", description = "管理员用户管理接口")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * 用户列表
     */
    @Operation(summary = "用户列表", description = "分页查询用户列表，支持搜索")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserVO>> getUserList(UserQueryDTO queryDTO) {
        PageResult<UserVO> result = userService.getUserList(queryDTO);
        return Result.success(result);
    }
}
