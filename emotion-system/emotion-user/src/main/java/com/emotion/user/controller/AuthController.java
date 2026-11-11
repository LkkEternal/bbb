package com.emotion.user.controller;

import com.emotion.common.annotation.OperationLog;
import com.emotion.common.response.Result;
import com.emotion.user.dto.*;
import com.emotion.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 负责用户认证相关操作：
 * - 用户注册
 * - 用户登录
 * - Token 刷新
 * - 用户退出
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     *
     * 流程说明：
     * 1. 验证用户名和邮箱是否已注册
     * 2. 加密密码
     * 3. 解析地址（自动获取城市ID、经纬度）
     * 4. 默认生成昵称（用户名后4位）
     * 5. 设置默认角色（1=普通用户）
     * 6. 记录注册时间
     *
     * @param request 注册请求，包含用户名、邮箱、密码、昵称、地址
     * @return 注册响应，包含用户ID、用户名、邮箱、昵称
     */
    @OperationLog(module = "auth", action = "register", description = "用户注册")
    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return Result.success("注册成功", response);
    }

    /**
     * 用户登录
     *
     * 流程说明：
     * 1. 根据账号查询用户
     * 2. 检查账号状态和锁定状态
     * 3. 验证密码
     * 4. 登录失败处理（防暴力破解）
     * 5. 更新最后登录信息
     * 6. 生成 JWT Token（访问令牌和刷新令牌）
     * 7. 存储刷新令牌到数据库和Redis
     *
     * 安全机制：
     * - 登录失败5次后锁定账号30分钟
     * - Token 包含用户ID、用户名、角色
     * - 刷新令牌存储哈希值
     *
     * @param request 登录请求，包含账号（用户名/邮箱）和密码
     * @param httpRequest HTTP请求对象，用于获取客户端IP和User-Agent
     * @return 登录响应，包含访问令牌、刷新令牌、过期时间、用户信息
     * @throws BizException 如果账号不存在、密码错误、账号被禁用或锁定
     */
    @OperationLog(module = "auth", action = "login", description = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, httpRequest);
        return Result.success("登录成功", response);
    }

    /**
     * 刷新访问令牌
     *
     * 流程说明：
     * 1. 从请求中获取刷新令牌
     * 2. 验证刷新令牌（Redis缓存或数据库）
     * 3. 验证用户状态
     * 4. 生成新的访问令牌
     * 5. 返回新的Token
     *
     * @param request 刷新令牌请求，包含刷新令牌
     * @return 登录响应，包含新的访问令牌和过期时间
     * @throws BizException 如果刷新令牌无效或账号被禁用
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request);
        return Result.success("刷新成功", response);
    }

    /**
     * 用户退出登录
     *
     * 流程说明：
     * 1. 从请求中提取Token
     * 2. 验证Token有效性
     * 3. 撤销所有刷新令牌（数据库）
     * 4. 清除Redis中的刷新令牌缓存
     *
     * @param request HTTP请求对象，包含Authorization头
     * @return 成功响应
     */
    @OperationLog(module = "auth", action = "logout", description = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return Result.success("退出成功", null);
    }
}
