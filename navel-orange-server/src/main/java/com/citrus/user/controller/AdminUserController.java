package com.citrus.user.controller;

import com.citrus.common.result.PageResult;
import com.citrus.common.result.Result;
import com.citrus.user.dto.UpdateUserStatusDTO;
import com.citrus.user.dto.UserQueryDTO;
import com.citrus.user.service.UserService;
import com.citrus.user.vo.UserDetailVO;
import com.citrus.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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

    /**
     * 用户详情
     */
    @Operation(summary = "用户详情", description = "查看用户详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDetailVO> getUserById(@PathVariable("id") Long id) {
        UserDetailVO result = userService.getUserById(id);
        return Result.success(result);
    }

    /**
     * 更新用户状态
     */
    @Operation(summary = "更新用户状态", description = "启用或禁用用户账号")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable("id") Long id,
                                          @RequestBody @Valid UpdateUserStatusDTO dto) {
        userService.updateUserStatus(id, dto.getStatus());
        return Result.success();
    }

    /**
     * 分配角色
     */
    @Operation(summary = "分配角色", description = "为用户分配角色")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
}
